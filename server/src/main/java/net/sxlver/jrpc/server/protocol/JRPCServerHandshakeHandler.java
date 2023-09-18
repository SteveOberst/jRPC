package net.sxlver.jrpc.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sxlver.jrpc.core.protocol.JRPCHandshake;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientHandshakeMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.protocol.packet.HandshakeStatusPacket;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;
import net.sxlver.jrpc.core.util.StringUtil;
import net.sxlver.jrpc.server.JRPCServer;
import org.jetbrains.annotations.NotNull;

public class JRPCServerHandshakeHandler extends SimpleChannelInboundHandler<JRPCClientHandshakeMessage> {

    private final JRPCServer server;

    public JRPCServerHandshakeHandler(JRPCServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext context, final JRPCClientHandshakeMessage message) {
        final byte[] data = message.data();
        final JRPCHandshake handshake = PacketDataSerializer.deserialize(data, JRPCHandshake.class);
        final HandshakeStatusPacket handshakeStatus = server.handshake(context.pipeline(), handshake);
        final JRPCServerChannelHandler handler = (JRPCServerChannelHandler) context.pipeline().get("message_handler");

        if(!handshakeStatus.getStatus()) {
            // build and send response with error description
            final JRPCMessage response = buildHandshakeResponse(handshake, handshakeStatus);
            handler.write(response);
            // don't let the message handler receive any more data when the handshake has failed as this
            // could lead to unexpected errors
            context.channel().pipeline().remove("message_handler");
            // handshake failed, close the channel
            context.channel().close().syncUninterruptibly();
            server.getLogger().info("Client failed authentication. Closing Connection... [Source: {}] [Auth Key: {}]", message.source(), StringUtil.cypherString(handshake.getToken()));
        }else {
            server.getLogger().info("Client {} successfully authenticated with the network.", message.source());
        }

        final JRPCMessage response = buildHandshakeResponse(handshake, handshakeStatus);
        handler.write(response);
        context.channel().pipeline().remove("handshake_handler");
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) {
        context.fireChannelActive();
    }

    private JRPCMessage buildHandshakeResponse(final JRPCHandshake handshake, final HandshakeStatusPacket response) {
        return JRPCMessageBuilder.builder()
                .target(handshake.getUniqueId())
                .targetType(Message.TargetType.DIRECT)
                .source(server)
                .data(PacketDataSerializer.serialize(response))
                .build();
    }
}
