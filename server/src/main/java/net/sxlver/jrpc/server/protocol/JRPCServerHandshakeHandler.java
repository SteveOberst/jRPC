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
import net.sxlver.jrpc.server.model.JRPCClientInstance;

public class JRPCServerHandshakeHandler extends SimpleChannelInboundHandler<JRPCClientHandshakeMessage> {

    private final JRPCServer server;

    public JRPCServerHandshakeHandler(JRPCServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext context, final JRPCClientHandshakeMessage message) {
        final byte[] data = message.data();
        final JRPCHandshake auth = PacketDataSerializer.deserialize(data, JRPCHandshake.class);
        if(!server.handshake(context.channel().remoteAddress(), auth)) {
            final JRPCClientInstance client = server.getBySource(context.channel().remoteAddress());
            final JRPCServerChannelHandler handler = client.getNetHandler();
            final JRPCMessage packet = JRPCMessageBuilder.builder()
                    .target(handler.getUniqueId())
                    .targetType(Message.TargetType.DIRECT)
                    .source(server)
                    .data(PacketDataSerializer.serialize(new HandshakeStatusPacket()))
                    .build();

            handler.write(packet);
            context.channel().closeFuture();
            server.getLogger().info("Client failed authentication. Closing Connection... [Source: {}] [Auth Key: {}]", client.getUniqueId(), StringUtil.cypherString(auth.getToken()));
        }else {
            server.getLogger().info("Client {} successfully authenticated with the network.", message.source());
        }
        context.channel().pipeline().remove("handshake_handler");

    }
}
