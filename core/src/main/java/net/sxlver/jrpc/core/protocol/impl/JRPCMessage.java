package net.sxlver.jrpc.core.protocol.impl;

import net.sxlver.jrpc.core.protocol.Message;

public class JRPCMessage implements Message {
    private String target;
    private TargetType targetType;
    private String source;
    private byte[] data;

    JRPCMessage(String target, TargetType targetType, String source, byte[] data) {
        this.source = source;
        this.target = target;
        this.targetType = targetType;
        this.data = data;
    }

    @Override
    public String source() {
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
    public byte[] data() {
        return data;
    }
}
