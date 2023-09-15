package net.sxlver.jrpc.core.protocol.impl;

import net.sxlver.jrpc.core.protocol.Message;

public record JRPCClientHandshakeMessage(String source, byte[] data) implements Message {

    @Override
    public String getTarget() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TargetType getTargetType() {
        throw new UnsupportedOperationException();
    }
}
