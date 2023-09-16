package net.sxlver.jrpc.core.protocol.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;

@NoArgsConstructor
public class JRPCClientHandshakeMessage implements Message {

    private String source;
    private byte[] data;

    public JRPCClientHandshakeMessage(final @NonNull String source, final byte[] data) {
        this.source = source;
        this.data = data;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public byte[] data() {
        return data;
    }

    @Override
    public String target() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TargetType targetType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConversationUID conversationId() {
        throw new UnsupportedOperationException();
    }
}
