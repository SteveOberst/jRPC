package net.sxlver.jrpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sxlver.jrpc.common.protocol.JRPCMessage;
import org.jetbrains.annotations.NotNull;

public class JRPCServerChannelHandler extends SimpleChannelInboundHandler<JRPCMessage> {
    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) throws Exception {

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) throws Exception {

    }
}
