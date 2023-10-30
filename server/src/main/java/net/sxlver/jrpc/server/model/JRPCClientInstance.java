package net.sxlver.jrpc.server.model;

import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import net.sxlver.jrpc.server.protocol.JRPCServerChannelHandler;

import java.net.InetSocketAddress;

public class JRPCClientInstance {
    private final JRPCServerChannelHandler handler;

    @Getter
    private final JRPCClientInformation information;

    public JRPCClientInstance(final @NonNull JRPCServerChannelHandler handler) {
        this.handler = handler;

        final boolean hideIps = handler.getServer().getConfig().isHideIpsFromClients();
        final InetSocketAddress address = hideIps ? handler.getServer().getLocalAddress() : handler.getRemoteAddress();
        this.information = new JRPCClientInformation(handler.getUniqueId(), handler.getType(), hideIps, address.getHostName(), address.getPort());
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
}
