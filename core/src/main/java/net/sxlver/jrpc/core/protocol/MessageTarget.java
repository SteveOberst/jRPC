package net.sxlver.jrpc.core.protocol;

public record MessageTarget(Message.TargetType targetType, String target) {
}
