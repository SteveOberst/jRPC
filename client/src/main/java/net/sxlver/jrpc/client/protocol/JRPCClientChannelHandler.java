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
import net.sxlver.jrpc.core.util.TimedCache;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class JRPCClientChannelHandler extends SimpleChannelInboundHandler<JRPCMessage> {

    private final JRPCClient client;
    private Channel channel;

    private final TimedCache<ConversationUID, Conversation<? extends Packet, ? extends Packet>> conversationObservers;
    private volatile boolean handshaked;

    public JRPCClientChannelHandler(final JRPCClient client) {
        this.client = client;
        this.conversationObservers = new TimedCache<>();
    }

    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) {
        client.publishMessage(message);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        client.getLogger().warn("An Exception has reached the end of pipeline: {}: {}", cause.getClass(), cause.getMessage());
        client.getLogger().fatal(cause);
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

    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> write(
            final @NonNull TRequest packet,
            final @NonNull MessageTarget target,
            final @Nullable Class<TResponse> expectedResponse,
            final @Nullable ConversationUID conversationUID) {

        final ConversationUID uid = conversationUID == null ? ConversationUID.newUid() : conversationUID;
        final JRPCMessage message = JRPCMessageBuilder.builder()
                .source(client)
                .target(target.target())
                .targetType(target.type())
                .conversationUid(uid)
                .data(PacketDataSerializer.serialize(packet))
                .build();

        channel.writeAndFlush(message);
        logPacketDispatch(packet, target, uid, message);
        return newConversation(packet, message.conversationId(), expectedResponse);
    }

    private <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> newConversation(
            final @NonNull TRequest request,
            final @NonNull ConversationUID uid,
            final @Nullable Class<? extends Packet> expectedResponse) {

        if(expectedResponse == null) {
            return Conversation.empty();
        }

        Conversation<TRequest, TResponse> conversation = new Conversation<>(request, uid, expectedResponse);
        this.conversationObservers.put(uid, conversation);
        return conversation;
    }

    private <TRequest extends Packet> void logPacketDispatch(final TRequest packet, final MessageTarget target, final ConversationUID uid, final JRPCMessage message) {
        client.getLogger().debug(
                "Sent packet {} to target {}. [Conversation UID: {}] [Target Type: {}] [Content Length. {}]",
                packet.getClass(), target.target(), uid.uid(), target.type().toString(), message.data().length
        );
    }

    public boolean isObserverPresent(final ConversationUID uid) {
        return getObserver(uid) != null;
    }

    @Nullable
    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> getObserver(final ConversationUID uid) {
        return (Conversation<TRequest, TResponse>) conversationObservers.get(uid);
    }

    public void invalidateConversation(final @NonNull ConversationUID id) {
        conversationObservers.remove(id);
    }

    @NotNull
    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> getAndInvalidateObserver(final ConversationUID uid) {
        final Conversation<TRequest, TResponse> conversation = getObserver(uid);
        Objects.requireNonNull(conversation);
        conversationObservers.remove(uid);
        return conversation;
    }

    private long waitingSince;
    public void awaitHandshakeResponse() {
        try {
            final Thread awaitingThread = new Thread(() -> {
                this.waitingSince = System.currentTimeMillis();
                while (!handshaked) {
                    if(System.currentTimeMillis() - waitingSince > client.getConfig().getConversationTimeOut()) {
                        client.getLogger().fatal("Didn't receive a handshake response.");
                        if(!client.getConfig().isIgnoreHandshakeResult()) {
                            break;
                        }
                        client.getLogger().info("Trying to continue anyways...");
                        ((JRPCClientHandshakeHandler) channel.pipeline().get("handshake_handler")).finish();
                        handshaked = true;
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
