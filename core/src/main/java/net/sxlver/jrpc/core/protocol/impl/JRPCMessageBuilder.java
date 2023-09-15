package net.sxlver.jrpc.core.protocol.impl;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageBuilder;

public class JRPCMessageBuilder implements MessageBuilder<JRPCMessage> {


    public JRPCMessage build() {
        return null;
    }

    @Override
    public MessageBuilder<JRPCMessage> data(byte[] data) {
        return null;
    }

    @Override
    public MessageBuilder<JRPCMessage> target(@NonNull String target) {
        return null;
    }

    @Override
    public MessageBuilder<JRPCMessage> targetType(Message.@NonNull TargetType targetType) {
        return null;
    }
}
