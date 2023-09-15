package net.sxlver.jrpc.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sxlver.jrpc.core.protocol.JRPCAuthentication;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientAuthMessage;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;
import net.sxlver.jrpc.server.JRPCServer;

public class JRPCServerAuthHandler extends SimpleChannelInboundHandler<JRPCClientAuthMessage> {

    private final JRPCServer server;

    public JRPCServerAuthHandler(JRPCServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext context, final JRPCClientAuthMessage message) throws Exception {
        final byte[] data = message.data();
        final JRPCAuthentication auth = PacketDataSerializer.deserialize(data, JRPCAuthentication.class);
        if(!server.authenticate(context.channel().remoteAddress(), auth)) {
            server.write();
            context.channel().closeFuture();
        }
    }
}
