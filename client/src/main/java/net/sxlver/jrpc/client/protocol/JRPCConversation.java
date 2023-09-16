package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;

public class JRPCConversation<T extends Message> {
    private JRPCResponseReceiver<T> responseReceiver = response -> {};
    private ConversationUID conversationUID;

    public JRPCConversation(final @NonNull JRPCResponseReceiver<T> responseReceiver, final @NonNull ConversationUID conversationUID) {
        this.responseReceiver = responseReceiver;
        this.conversationUID = conversationUID;
    }

    public ConversationUID getConversationUID() {
        return conversationUID;
    }

    void processResponse(final T response) {
        responseReceiver.onReceive(response);
    }

    public JRPCConversation<T> onResponse(final JRPCResponseReceiver<T> responseReceiver) {
        this.responseReceiver = responseReceiver;
        return this;
    }
}
