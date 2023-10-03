package net.sxlver.jrpc.core.protocol.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageType;
import net.sxlver.jrpc.core.protocol.ProtocolVersion;

@NoArgsConstructor
public class JRPCClientHandshakeMessage extends JRPCMessage {

    public JRPCClientHandshakeMessage(final @NonNull String source, final byte[] data) {
       super("", TargetType.SERVER, source, data, ProtocolVersion.V0_1.getVersionNumber(), MessageType.HANDSHAKE.getId());
    }


    @Override
    public String target() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message.TargetType targetType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConversationUID conversationId() {
        throw new UnsupportedOperationException();
    }
}
