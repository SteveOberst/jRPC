package net.sxlver.jrpc.core.protocol.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageType;
import net.sxlver.jrpc.core.protocol.ProtocolVersion;
import net.sxlver.jrpc.core.util.TimedCache;
import net.sxlver.jrpc.core.util.TimedQueue;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class JRPCMessage extends HeaderData implements Message, TimedQueue.NotifyOnExpire {
    private String target;
    private TargetType targetType;
    private String source;
    private ConversationUID conversationUID;
    private byte[] data;

    private long queueTimeout;

    JRPCMessage(final @NonNull String target, final @NonNull TargetType targetType, final @NonNull String source, final byte[] data) {
        this(target, targetType, source, ConversationUID.newUid(), data);
    }

    JRPCMessage(final @NonNull String target,
                final @NonNull TargetType targetType,
                final @NonNull String source,
                final @NonNull ConversationUID conversationUID,
                final byte[] data) {

        this(target, targetType, source, conversationUID, data, ProtocolVersion.V0_1.getVersionNumber(), MessageType.MESSAGE.getId()); // TODO: fetch right protocol version number
    }

    JRPCMessage(final @NonNull String target,
                final @NonNull TargetType targetType,
                final @NonNull String source,
                final byte[] data,
                final int protocolVersion,
                final int messageType) {

        this(target, targetType, source, ConversationUID.newUid(), data, protocolVersion, messageType);
    }

    JRPCMessage(final @NonNull String target,
                final @NonNull TargetType targetType,
                final @NonNull String source,
                final ConversationUID conversationUID,
                final byte[] data,
                final int protocolVersion,
                final int messageType) {

        super(protocolVersion, messageType);
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

    @Override
    public void notifyExpired() {}

    public void setQueueTimeout(final long timeout) {
        this.queueTimeout = timeout;
    }

    @Override
    public long timeout() {
        return queueTimeout;
    }
}
