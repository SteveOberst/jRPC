package net.sxlver.jrpc.core.protocol.impl;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.DataSource;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageBuilder;

public class JRPCMessageBuilder implements MessageBuilder<JRPCMessage> {

    private String target;
    private Message.TargetType targetType;
    private DataSource dataSource;
    private ConversationUID conversationUID;
    private byte[] data;

    private JRPCMessageBuilder() {}

    public static MessageBuilder<JRPCMessage> builder() {
        return new JRPCMessageBuilder();
    }

    @Override
    public MessageBuilder<JRPCMessage> target(final @NonNull String target) {
        this.target = target;
        return this;
    }

    @Override
    public MessageBuilder<JRPCMessage> targetType(final @NonNull Message.TargetType targetType) {
        this.targetType = targetType;
        return this;
    }

    @Override
    public MessageBuilder<JRPCMessage> source(final @NonNull DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    @Override
    public MessageBuilder<JRPCMessage> conversationUid(final @NonNull ConversationUID uid) {
        this.conversationUID = uid;
        return this;
    }

    @Override
    public MessageBuilder<JRPCMessage> data(final byte[] data) {
        this.data = data;
        return this;
    }

    public JRPCMessage build() {
        if(!canBuild()) throw new IllegalStateException("Builder incomplete");
        return new JRPCMessage(target, targetType, dataSource.getSource(), data);
    }

    private boolean canBuild() {
        return data != null && target != null && targetType != null && dataSource != null;
    }
}
