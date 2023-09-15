package net.sxlver.jrpc.client.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.Errors;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.protocol.packet.ErrorResponsePacket;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

public class JRPCClientChannelHandler extends SimpleChannelInboundHandler<JRPCMessage> {

    private final JRPCClient client;

    public JRPCClientChannelHandler(final JRPCClient client) {
        this.client = client;
    }

    @Override
    public void channelRead0(final @NotNull ChannelHandlerContext context, final @NotNull JRPCMessage message) {

    }



    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        client.getLogger().warn("An Exception has reached the end of pipeline: {}", cause.getMessage());
        client.getLogger().debug(ExceptionUtils.getStackTrace(cause));
    }
}
