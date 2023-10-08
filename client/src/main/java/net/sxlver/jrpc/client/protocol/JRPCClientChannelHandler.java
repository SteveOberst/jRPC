package net.sxlver.jrpc.client.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
import net.sxlver.jrpc.core.util.TimedCache;
import net.sxlver.jrpc.core.util.TimedQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketException;

public class JRPCClientChannelHandler extends SimpleChannelInboundHandler<JRPCMessage> {

    private final JRPCClient client;
    private Channel channel;

    private static final TimedCache<ConversationUID, Conversation<? extends Packet, ? extends Packet>> conversationObservers = new TimedCache<>();
    private static final TimedQueue<Conversation<? extends Packet, ? extends Packet>, JRPCMessage> queuedMessages = new TimedQueue<>();
    private volatile boolean handshaked;

    public JRPCClientChannelHandler(final JRPCClient client) {
        this.client = client;
    }

    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) {
        client.publishToHandlers(message);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        if(cause instanceof ReadTimeoutException) {
            client.getLogger().warn("{}: {}", cause.getClass().getSimpleName(), cause.getMessage());
            return;
        }

        if(cause instanceof SocketException) {
            client.getLogger().warn("{}: {}", cause.getClass().getSimpleName(), cause.getMessage());
            return;
        }

        client.getLogger().warn("An Exception has reached the end of pipeline: {}: {}", cause.getClass(), cause.getMessage());
        client.getLogger().fatal(cause);
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) {
        this.channel = context.channel();
        client.getLogger().debugFine("A connection has been established.");
        context.fireChannelActive();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext context) {
        client.onChannelClose(context);
        handshaked = false;
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

        final boolean channelActive = channel != null && channel.isActive();
        final Conversation<TRequest, TResponse> conversation = newConversation(packet, message.conversationId(), expectedResponse, channelActive);
        if(channelActive) {
            channel.writeAndFlush(message);
            logPacketDispatch(packet, target, uid, message);
        } else {
            queueMessage(conversation, message);
        }
        return conversation;
    }

    private <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> newConversation(
            final @NonNull TRequest request,
            final @NonNull ConversationUID uid,
            final @Nullable Class<? extends Packet> expectedResponse,
            final boolean registerConversation) {

        if(expectedResponse == null) {
            return Conversation.empty();
        }

        Conversation<TRequest, TResponse> conversation = new Conversation<>(client, request, uid, expectedResponse);
        if(registerConversation) conversationObservers.put(uid, conversation);
        return conversation;
    }

    private <TRequest extends Packet> void logPacketDispatch(final TRequest packet, final MessageTarget target, final ConversationUID uid, final JRPCMessage message) {
        client.getLogger().debugFiner(
                "Sent packet {} to target {}. [Conversation UID: {}] [Target Type: {}] [Content Length. {}]",
                packet.getClass(), target.target(), uid.uid(), target.type().toString(), message.data().length
        );
    }

    private void pollQueue() {
        queuedMessages.forEach((conversation, message) -> {
            conversationObservers.put(conversation.getConversationUID(), conversation);
            channel.writeAndFlush(message);
            client.getLogger().debugFiner("Sent queued message for {} with conversation id {}", conversation.getRequest().getClass(), conversation.getConversationUID());
        });
    }

    private void queueMessage(final Conversation<? extends Packet, ? extends Packet> conversation, final JRPCMessage message) {
        message.setQueueTimeout(client.getConfig().getQueuedMessageTimeout() * 1000L);
        queuedMessages.enqueue(conversation, message);
        client.getLogger().debugFiner("Message for request {} with conversation id {} has been queued because the channel is inactive.", conversation.getRequest().getClass(), conversation.getConversationUID());
        client.getLogger().debugFinest("Current message queue: {}", queuedMessages);
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
            if(client.getConfig().isQueueMessages()) {
                client.getLogger().debugFine("Polling message queue...");
                pollQueue();
            }
        }catch(final InterruptedException exception) {
            client.getLogger().fatal("A Fatal error occurred whilst creating thread to await handshake. This error is not recoverable.");
        }
    }
}
