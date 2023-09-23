package net.sxlver.jrpc.server.protocol;

import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.*;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationPacket;
import net.sxlver.jrpc.core.protocol.packet.KeepAlivePacket;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
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
    private String uniqueId = "empty";
    private String type = "empty";

    private long lastWrite = System.currentTimeMillis();

    private KeepAlivePacket lastKeepAlive = new KeepAlivePacket();

    public JRPCServerChannelHandler(final JRPCServer server) {
        this.server = server;
    }

    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) {
        // has the client authenticated yet?
        if(!handshaked) {
            // Client is not yet authenticated, ignore request and respond with an error
            write(new ErrorInformationPacket(Errors.ERR_NOT_AUTHENTICATED, "Client is not authenticated.", null));
            return;
        }

        // TODO: think of another solution than extracting the class path every time as this
        //       is a little I/O heavy
        //
        // Preserve JsonObject in first step to save up on some I/O load
        final JsonObject jsonObject = PacketDataSerializer.deserializeJson(message.data());
        if(PacketDataSerializer.extractClassPath(jsonObject).equals(KeepAlivePacket.class.getName())) {
            // Finally deserialize the JsonObject to the KeepAlivePacket class instance
            final KeepAlivePacket packet = PacketDataSerializer.deserialize(jsonObject, KeepAlivePacket.class);
            final JRPCClientInstance client = server.getByUniqueId(message.source());
            client.onKeepAlive(context, lastKeepAlive, packet);
            sendKeepAlive();
            return;
        }

        if(message.targetType() != Message.TargetType.SERVER) {
            // forward message to target client instance(s)
            server.forward(message, this);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        if(cause instanceof ReadTimeoutException) {
            server.getLogger().info("Connection to client with id {} has timed out.", client.getUniqueId());
            return;
        }
        server.getLogger().warn("An unhandled Exception has reached the end of pipeline: {}: {}", cause.getClass(), cause.getMessage());
        server.getLogger().fatal(ExceptionUtils.getStackTrace(cause));
    }

    @Override
    public void channelActive(final @NotNull ChannelHandlerContext context) {
        this.channel = context.channel();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        server.removeConnected(client);
        server.getLogger().info("The Connection to {} has been closed.", client.getUniqueId());
    }



    public boolean onHandshakeSuccess(final JRPCHandshake handshake) {
        this.uniqueId = handshake.getUniqueId();
        this.type = handshake.getType();
        this.client = new JRPCClientInstance(this);
        server.addConnected(client);
        return (this.handshaked = handshake.getToken().equals(server.getConfig().getAuthenticationToken()));
    }

    public void write(final Packet packet) {
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

    public void sendKeepAlive() {
        final KeepAlivePacket packet = new KeepAlivePacket();
        this.lastKeepAlive = packet;
        write(packet);
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
