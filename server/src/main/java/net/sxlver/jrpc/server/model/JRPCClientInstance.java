package net.sxlver.jrpc.server.model;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import net.sxlver.jrpc.core.protocol.packet.KeepAlivePacket;
import net.sxlver.jrpc.server.protocol.JRPCServerChannelHandler;

public class JRPCClientInstance {
    private final JRPCServerChannelHandler handler;

    @Getter
    private final JRPCClientInformation information;

    @Getter
    private long ping;

    public JRPCClientInstance(final @NonNull JRPCServerChannelHandler handler) {
        this.handler = handler;
        this.information = new JRPCClientInformation(handler.getUniqueId(), handler.getType(), handler.getRemoteAddress().getHostName(), handler.getRemoteAddress().getPort());
    }

    public JRPCServerChannelHandler getNetHandler() {
        return handler;
    }

    public long getLastWrite() {
        return handler.getLastWrite();
    }

    public String getUniqueId() {
        return information.getUniqueId();
    }

    public String getType() {
        return information.getType();
    }

    public void onKeepAlive(final ChannelHandlerContext context, final KeepAlivePacket sent, final KeepAlivePacket received) {
        this.ping = System.currentTimeMillis() - sent.getTimestamp();
    }
}
