package net.sxlver.jrpc.client.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.*;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class JRPCClientChannelHandler extends SimpleChannelInboundHandler<JRPCMessage> {

    private final JRPCClient client;
    private Channel channel;

    private Cache<ConversationUID, Conversation<? extends Packet, ? extends Packet>> conversationObservers;
    private volatile boolean handshaked;

    public JRPCClientChannelHandler(final JRPCClient client) {
        this.client = client;
        this.conversationObservers = CacheBuilder.newBuilder().expireAfterWrite(client.getConfig().getConversationTimeOut(), TimeUnit.MILLISECONDS).build();
    }

    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) {
        client.publishMessage(message);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        client.getLogger().warn("An Exception has reached the end of pipeline: {}: {}", cause.getClass(), cause.getMessage());
        client.getLogger().fatal(ExceptionUtils.getStackTrace(cause));
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) {
        this.channel = context.channel();
        client.getLogger().info("Channel opened.");
        context.fireChannelActive();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext context) {
        this.channel = null;
        client.onChannelClose(context);
        context.fireChannelInactive();
    }

    public void setHandshaked(final boolean handshaked) {
        this.handshaked = handshaked;
    }

    public <Request extends Packet, Response extends Packet> Conversation<Request, Response> write(
            final @NonNull Request packet,
            final @NonNull MessageTarget target,
            final @Nullable Class<Response> expectedResponse,
            final @Nullable ConversationUID conversationUID
    ) {
        final ConversationUID uid = conversationUID == null ? ConversationUID.newUid() : conversationUID;
        final JRPCMessage message = JRPCMessageBuilder.builder()
                .source(client)
                .target(target.target())
                .targetType(target.targetType())
                .conversationUid(uid)
                .data(PacketDataSerializer.serialize(packet))
                .build();

        channel.writeAndFlush(message);
        logPacketDispatch(packet, target, uid, message);
        return getOrCreate(packet, message.conversationId(), expectedResponse);
    }

    private <Request extends Packet, Response extends Packet> Conversation<Request, Response> getOrCreate(
            final @NonNull Request request,
            final @NonNull ConversationUID uid,
            final @Nullable Class<? extends Packet> expectedResponse
    ) {
        if(expectedResponse == null) {
            return Conversation.empty();
        }

        Conversation<Request, Response> conversation;
        if((conversation = getObserver(uid)) != null) {
            return conversation;
        }

        conversation = new Conversation<>(request, uid, expectedResponse);
        this.conversationObservers.put(uid, conversation);
        return conversation;
    }

    private <Request extends Packet> void logPacketDispatch(final Request packet, final MessageTarget target, final ConversationUID uid, final JRPCMessage message) {
        client.getLogger().debug(
                "Sent packet {} to target {}. [Conversation UID: {}] [Target Type: {}] [Content Length. {}]",
                packet.getClass(), target.target(), uid.uid(), target.targetType().toString(), message.data().length
        );
    }

    public boolean isObserverPresent(final ConversationUID uid) {
        return getObserver(uid) != null;
    }

    @Nullable
    public <Request extends Packet, Response extends Packet> Conversation<Request, Response> getObserver(final ConversationUID uid) {
        return (Conversation<Request, Response>) conversationObservers.getIfPresent(uid);
    }

    @NotNull
    public <Request extends Packet, Response extends Packet> Conversation<Request, Response> getAndInvalidateObserver(final ConversationUID uid) {
        final Conversation<Request, Response> conversation = getObserver(uid);
        Objects.requireNonNull(conversation);
        conversationObservers.invalidate(uid);
        return conversation;
    }

    private long waitingSince;
    public void awaitHandshakeResponse() {
        try {
            final Thread awaitingThread = new Thread(() -> {
                this.waitingSince = System.currentTimeMillis();
                while (!handshaked) {
                    if(System.currentTimeMillis() - waitingSince > client.getConfig().getConversationTimeOut()) {
                        client.getLogger().fatal("Didn't receive a handshake response. Trying to continue anyways...");
                        break;
                    }
                }
            }, "Handshake-Daemon");
            client.getLogger().info("Awaiting handshake response...");
            awaitingThread.start();
            awaitingThread.join();
        }catch(final InterruptedException exception) {
            client.getLogger().fatal("A Fatal error occurred whilst creating thread to await handshake. This error is not recoverable.");
        }
    }
}
