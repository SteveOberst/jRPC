package net.sxlver.jrpc.core.protocol;

import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;

public class MessageTarget {

    private final Message.TargetType targetType;
    private final String target;

    public MessageTarget(Message.TargetType targetType) {
        this(targetType, "");
    }

    public MessageTarget(Message.TargetType targetType, String target) {
        this.targetType = targetType;
        this.target = target;
    }

    public String target() {
        return target;
    }

    public Message.TargetType type() {
        return targetType;
    }

    public static MessageTarget of(final JRPCClientInformation client) {
        return new MessageTarget(Message.TargetType.DIRECT, client.getUniqueId());
    }
}
