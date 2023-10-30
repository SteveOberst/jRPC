package net.sxlver.jrpc.server.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.*;
import net.sxlver.jrpc.core.protocol.Errors;
import net.sxlver.jrpc.core.protocol.impl.JRPCHandshake;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationResponse;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
import net.sxlver.jrpc.server.JRPCServer;
import net.sxlver.jrpc.server.model.JRPCClientInstance;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketException;

public class JRPCServerChannelHandler extends SimpleChannelInboundHandler<JRPCMessage> {

    private final JRPCServer server;
    private Channel channel;

    private JRPCClientInstance client;

    private boolean handshaked;
    private String uniqueId = "empty";
    private String type = "empty";

    private long lastWrite = System.currentTimeMillis();


    public JRPCServerChannelHandler(final JRPCServer server) {
        this.server = server;
    }

    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) {
        // has the client authenticated yet?
        if(!handshaked) {
            // Client is not yet authenticated, ignore request and respond with an error
            write(new ErrorInformationResponse(Errors.ERR_NOT_AUTHENTICATED, "Client is not authenticated."));
            channel.close();
            server.getLogger().warn("Remote client {} has sent data before authenticating, closing connection...", channel.remoteAddress());
            return;
        }

        if(message.targetType() == Message.TargetType.SERVER) {
            final Packet packet = PacketDataSerializer.deserializePacket(message.data());
            server.onReceive(this, message, packet);
        }
        else {
            // forward message to target client instance(s)
            server.forward(message, this);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        if(client == null) return;
        if(cause instanceof ReadTimeoutException) {
            server.getLogger().info("Connection to client with id {} has timed out.", client.getUniqueId());
            close();
            return;
        }

        if(cause instanceof SocketException) {
            server.getLogger().info("Client '{}' has unexpectedly closed the connection.", client.getUniqueId());
            close();
            return;
        }

        server.getLogger().warn("An unhandled Exception has reached the end of pipeline: {}: {}", cause.getClass(), cause.getMessage());
        server.getLogger().fatal(cause);
    }

    @Override
    public void channelActive(final @NotNull ChannelHandlerContext context) {
        this.channel = context.channel();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        if(client == null || !handshaked) {
            server.getLogger().info("An unauthenticated client has closed the connection. Remote address: {}", getRemoteAddress());
            return;
        }
        close();
    }

    private void close() {
        if(channel.isActive() || server.isConnected(client)) {
            channel.close();
            server.removeConnected(client);
            server.getLogger().info("The Connection to {} has been closed.", client.getUniqueId());
        }
    }

    public boolean onHandshakeSuccess(final JRPCHandshake handshake) {
        this.uniqueId = handshake.getUniqueId();
        this.type = handshake.getType();
        this.client = new JRPCClientInstance(this);

        final boolean success = (this.handshaked = handshake.getToken().equals(server.getConfig().getAuthenticationToken()));
        if(success) {
            server.addConnected(client);
        }
        return success;
    }

    public void write(final Packet packet) {
        write(packet, ConversationUID.newUid());
    }

    public void write(final Packet packet, final @NonNull ConversationUID sourceConversation) {
        final JRPCMessage message = JRPCMessageBuilder.builder()
                .source(server)
                .target(uniqueId)
                .conversationUid(sourceConversation)
                .targetType(Message.TargetType.DIRECT)
                .data(PacketDataSerializer.serialize(packet))
                .build();

        write(message);
    }

    public void write(final @NonNull JRPCMessage message) {
        this.lastWrite = System.currentTimeMillis();
        channel.writeAndFlush(message);
    }

    public void shutdown() {
        channel.closeFuture().awaitUninterruptibly();
    }

    public JRPCServer getServer() {
        return server;
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getType() {
        return type;
    }

    public long getLastWrite() {
        return lastWrite;
    }
}
