package net.sxlver.jrpc.core.protocol;

public class MessageTarget {

    private Message.TargetType targetType;
    private String target;

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
}
