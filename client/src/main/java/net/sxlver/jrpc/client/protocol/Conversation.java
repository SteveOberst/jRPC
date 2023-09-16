package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Packet;


public class Conversation<T extends Packet> {
    private ResponseReceiver<T> responseReceiver = packet -> {};
    private ConversationUID conversationUID;
    private Class<? extends Packet> expectedResponse;

    public Conversation(final @NonNull ConversationUID conversationUID, final @NonNull Class<? extends Packet> expectedResponse) {
        this.conversationUID = conversationUID;
        this.expectedResponse = expectedResponse;
    }

    public Conversation(final @NonNull ConversationUID conversationUID, final @NonNull Class<? extends Packet> expectedResponse, final @NonNull ResponseReceiver<T> responseReceiver) {
        this.conversationUID = conversationUID;
        this.expectedResponse = expectedResponse;
        this.responseReceiver = responseReceiver;
    }

    public ConversationUID getConversationUID() {
        return conversationUID;
    }

    public Class<? extends Packet> getExpectedResponse() {
        return expectedResponse;
    }

    void processResponse(final T response) {
        responseReceiver.onReceive(response);
    }

    public Conversation<T> onResponse(final ResponseReceiver<T> responseReceiver) {
        this.responseReceiver = responseReceiver;
        return this;
    }
}
