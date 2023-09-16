package net.sxlver.jrpc.client.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.*;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientHandshakeMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class JRPCClientChannelHandler extends SimpleChannelInboundHandler<JRPCMessage> {

    private final JRPCClient client;
    private Channel channel;

    private Cache<ConversationUID, JRPCConversation<Message>> rawConversationObservers;
    private Cache<ConversationUID, Conversation<? extends Packet>> conversationObservers;

    private volatile boolean handshaked;

    public JRPCClientChannelHandler(final JRPCClient client) {
        this.client = client;
        this.rawConversationObservers = CacheBuilder.newBuilder().expireAfterWrite(client.getConfig().getConversationTimeOut(), TimeUnit.MILLISECONDS).build();
        this.conversationObservers = CacheBuilder.newBuilder().expireAfterWrite(client.getConfig().getConversationTimeOut(), TimeUnit.MILLISECONDS).build();
    }

    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) {
        final ConversationUID uid = message.conversationId();
        final JRPCConversation<Message> rawObserver = rawConversationObservers.getIfPresent(uid);
        if(rawObserver != null) {
            rawObserver.processResponse(message);
        }

        client.publishMessage(message);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        client.getLogger().warn("An Exception has reached the end of pipeline: {}", cause.getMessage());
        client.getLogger().debug(ExceptionUtils.getStackTrace(cause));
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) {
        this.channel = context.channel();
        client.getLogger().info("Channel opened.");
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext context) {
        this.channel = null;
        client.onChannelClose(context);
    }

    public void setHandshaked(final boolean handshaked) {
        this.handshaked = true;
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> Conversation<T> write(final @NonNull T packet, final @NonNull MessageTarget target, Class<? extends Packet> expectedResponse, final ConversationUID conversationUID) {
        final ConversationUID uid = conversationUID == null ? ConversationUID.newUid() : conversationUID;
        final JRPCMessage message = JRPCMessageBuilder.builder()
                .source(client)
                .target(target.target())
                .targetType(target.targetType())
                .conversationUid(uid)
                .data(PacketDataSerializer.serialize(packet))
                .build();

        channel.writeAndFlush(message);
        final Conversation<T> conversation = newConversation(message.conversationId(), expectedResponse);
        this.conversationObservers.put(message.conversationId(), conversation);
        return conversation;
    }

    private <T extends Packet> Conversation<T> newConversation(final ConversationUID uid, final Class<? extends Packet> expectedResponse) {
        return new Conversation<>(uid, expectedResponse);
    }

    private long waitingSince;
    public void awaitHandshakeResponse() {
        try {
            final Thread awaitingThread = new Thread(() -> {
                waitingSince = System.currentTimeMillis();
                while (!handshaked) {
                    if(System.currentTimeMillis() - waitingSince > client.getConfig().getConversationTimeOut()) {
                        client.getLogger().fatal("Didn't receive a handshake response. Trying to continue anyways.");
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
