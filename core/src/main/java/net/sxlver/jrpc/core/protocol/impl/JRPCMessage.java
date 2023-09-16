package net.sxlver.jrpc.core.protocol.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;

@NoArgsConstructor
public class JRPCMessage implements Message {
    private String target;
    private TargetType targetType;
    private String source;
    private ConversationUID conversationUID;
    private byte[] data;

    JRPCMessage(final @NonNull String target, final @NonNull TargetType targetType, final @NonNull String source, final byte[] data) {
        this(target, targetType, source, ConversationUID.newUid(), data);
    }

    JRPCMessage(final @NonNull String target, final @NonNull TargetType targetType, final @NonNull String source, final ConversationUID conversationUID, final byte[] data) {
        this.source = source;
        this.target = target;
        this.targetType = targetType;
        this.conversationUID = conversationUID;
        this.data = data;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public String target() {
        return target;
    }

    @Override
    public TargetType targetType() {
        return targetType;
    }

    @Override
    public ConversationUID conversationId() {
        return conversationUID;
    }

    @Override
    public byte[] data() {
        return data;
    }
}
