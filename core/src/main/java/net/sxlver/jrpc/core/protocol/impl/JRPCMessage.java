package net.sxlver.jrpc.core.protocol.impl;

import net.sxlver.jrpc.core.protocol.Message;

public class JRPCMessage implements Message {
    private String source;
    private String target;
    private TargetType targetType;

    private byte[] data;

    public JRPCMessage(String source, String target, TargetType targetType, byte[] data) {
        this.source = source;
        this.target = target;
        this.targetType = targetType;
        this.data = data;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public TargetType getTargetType() {
        return targetType;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
