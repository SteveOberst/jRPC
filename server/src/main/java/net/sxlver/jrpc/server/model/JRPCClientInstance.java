package net.sxlver.jrpc.server.model;

import net.sxlver.jrpc.server.protocol.JRPCServerChannelHandler;

public class JRPCClientInstance {
    private JRPCServerChannelHandler handler;

    public JRPCClientInstance(JRPCServerChannelHandler handler) {
        this.handler = handler;
    }

    public JRPCServerChannelHandler getNetHandler() {
        return handler;
    }

    public String getUniqueId() {
        return handler.getUniqueId();
    }

    public String getType() {
        return handler.getType();
    }
}
