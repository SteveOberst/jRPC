package net.sxlver.jrpc.server.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Errors;
import net.sxlver.jrpc.core.protocol.JRPCHandshake;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.protocol.packet.ErrorResponsePacket;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;
import net.sxlver.jrpc.server.JRPCServer;
import net.sxlver.jrpc.server.model.JRPCClientInstance;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public class JRPCServerChannelHandler extends SimpleChannelInboundHandler<JRPCMessage> {

    private final JRPCServer server;
    private Channel channel;

    private JRPCClientInstance client;

    private boolean handshaked;
    private String uniqueId;
    private String type;

    private long lastWrite = System.currentTimeMillis();

    public JRPCServerChannelHandler(final JRPCServer server) {
        this.server = server;
    }

    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) {
        if(!handshaked) {
            write(new ErrorResponsePacket(Errors.ERR_NOT_AUTHENTICATED, "Client is not authenticated."));
            return;
        }

        server.forward(message, this);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        server.getLogger().warn("An Exception has reached the end of pipeline: {}", cause.getMessage());
        server.getLogger().debug(ExceptionUtils.getStackTrace(cause));
    }

    @Override
    public void channelActive(final @NotNull ChannelHandlerContext context) {
        this.channel = context.channel();
        this.client = new JRPCClientInstance(this);
        server.addConnected(client);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        server.removeConnected(client);
        server.getLogger().info("The Connection to {} has been closed.", client.getUniqueId());
    }

    private void write(final Packet packet) {
        final JRPCMessage message = JRPCMessageBuilder.builder()
                .source(server)
                .target(uniqueId)
                .targetType(Message.TargetType.DIRECT)
                .data(PacketDataSerializer.serialize(packet))
                .build();

        write(message);
    }

    public void write(final @NonNull JRPCMessage message) {
        this.lastWrite = System.currentTimeMillis();
        channel.writeAndFlush(message);
    }

    public boolean handshake(final JRPCHandshake handshake) {
        this.uniqueId = handshake.getUniqueId();
        this.type = handshake.getType();
        return (this.handshaked = handshake.getToken().equals(server.getConfig().getAuthenticationToken()));
    }

    public void shutdown() {
        channel.closeFuture().awaitUninterruptibly();
    }

    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
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
