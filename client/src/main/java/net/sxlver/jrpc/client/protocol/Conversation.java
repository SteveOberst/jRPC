package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Packet;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class Conversation<Request extends Packet, Response extends Packet> {

    private final Request request;
    private final ConversationUID conversationUID;
    private BiConsumer<Request, Response> responseConsumer;
    private Class<? extends Packet> expectedResponse;

    private static Conversation<?, ?> EMPTY = new Conversation<>();

    private Conversation() {
        this.request = null;
        this.conversationUID = null;
    }

    public Conversation(final @NonNull Request request, final @NonNull ConversationUID conversationUID, final @NonNull Class<? extends Packet> expectedResponse) {
        this(request, conversationUID, expectedResponse, (req, res) -> {});
    }

    public Conversation(final @NonNull Request request, final @NonNull ConversationUID conversationUID, final @NonNull Class<? extends Packet> expectedResponse, final @NonNull BiConsumer<Request, Response> consumer) {
        this.request = request;
        this.conversationUID = conversationUID;
        this.expectedResponse = expectedResponse;
        this.responseConsumer = consumer;
    }

    public ConversationUID getConversationUID() {
        return conversationUID;
    }

    public Class<? extends Packet> getExpectedResponse() {
        return expectedResponse;
    }

    void onResponse(final Response response) {
        responseConsumer.accept(request, response);
    }

    public Conversation<Request, Response> onResponse(final BiConsumer<Request, Response> consumer) {
        this.responseConsumer = consumer;
        return this;
    }

    @SuppressWarnings("unchecked")
    public static <Request extends Packet, Response extends Packet> Conversation<Request, Response> empty() {
        return (Conversation<Request, Response>) EMPTY;
    }
}
