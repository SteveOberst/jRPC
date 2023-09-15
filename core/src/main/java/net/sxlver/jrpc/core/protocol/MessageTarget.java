package net.sxlver.jrpc.core.protocol;

public class MessageTarget {
    private final Message.TargetType targetType;
    private final String target;

    public MessageTarget(Message.TargetType targetType, String target) {
        this.targetType = targetType;
        this.target = target;
    }

    public Message.TargetType getTargetType() {
        return targetType;
    }

    public String getTarget() {
        return target;
    }
}
