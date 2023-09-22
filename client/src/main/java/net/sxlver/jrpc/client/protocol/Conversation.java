package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.ErrorInformationHolder;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationPacket;

import java.util.function.BiConsumer;


/**
 * The type Conversation.
 *
 * @param <TRequest>  the type parameter
 * @param <TResponse> the type parameter
 */
public final class Conversation<TRequest extends Packet, TResponse extends Packet> {

    private final TRequest request;
    private final ConversationUID conversationUID;
    private Class<? extends Packet> expectedResponse;

    private BiConsumer<TRequest, TResponse> responseConsumer = (req, res) -> {};
    private BiConsumer<Throwable, ErrorInformationHolder> errorHandler = (throwable, errorInformationPacket) -> {};
    private boolean overrideHandlers;
    private boolean concurrentResponseProcessing;

    private static final Conversation<?, ?> EMPTY = new Conversation<>();

    private Conversation() {
        this.request = null;
        this.conversationUID = null;
    }

    /**
     * Instantiates a new Conversation.
     *
     * @param request          the request
     * @param conversationUID  the conversation uid
     * @param expectedResponse the expected response type
     */
    public Conversation(final @NonNull TRequest request, final @NonNull ConversationUID conversationUID, final @NonNull Class<? extends Packet> expectedResponse) {
        this.request = request;
        this.conversationUID = conversationUID;
        this.expectedResponse = expectedResponse;
    }

    /**
     * Gets conversation uid.
     *
     * @return the conversation uid
     */
    public ConversationUID getConversationUID() {
        return conversationUID;
    }

    /**
     * Gets expected response.
     *
     * @return the expected response
     */
    public Class<? extends Packet> getExpectedResponse() {
        return expectedResponse;
    }

    /**
     * Invokes the onResponse handler on the given parameter.
     *
     * @param response the response
     */
    void onResponse(final TResponse response) {
        responseConsumer.accept(request, response);
    }

    /**
     * Action to be run on the response when one was received.
     *
     * @param consumer the consumer
     * @return the conversation
     */
    public Conversation<TRequest, TResponse> onResponse(final @NonNull BiConsumer<TRequest, TResponse> consumer) {
        this.responseConsumer = consumer;
        return this;
    }

    boolean shouldOverrideHandlers() {
        return overrideHandlers;
    }

    /**
     * Whether the handler defined through {@link #onResponse(BiConsumer)} should intercept
     * the event and handle it exclusively, cancelling further action on the response by
     * excluding it from being passed to registered handlers.
     *
     * @return current instance of the Conversation Object
     */
    public Conversation<TRequest, TResponse> overrideHandlers() {
        this.overrideHandlers = true;
        return this;
    }

    void except(final @NonNull Throwable throwable, final ErrorInformationHolder packet) {
        errorHandler.accept(throwable, packet);
    }

    /**
     * Handler invoked when an error occurs on the other side, and they respond with an
     * {@link ErrorInformationPacket}. Throwable might just be an empty exception if the
     * error wasn't raised by an exception or the other end simply didn't provide an exception.
     *
     * @param <T>          the type parameter
     * @param errorHandler action to be run when an error response is received
     * @return current instance of the Conversation Object
     */
    @SuppressWarnings("unchecked")
    public <T extends ErrorInformationHolder> Conversation<TRequest, TResponse> onExcept(final @NonNull BiConsumer<Throwable, T> errorHandler) {
        this.errorHandler = (BiConsumer<Throwable, ErrorInformationHolder>) errorHandler;
        return this;
    }

    boolean isConcurrentResponseProcessing() {
        return concurrentResponseProcessing;
    }

    /**
     * Enable processing of concurrent responses for this conversation.
     * One request could trigger many client instances to respond instead of just one.
     *
     * <p>setting this option to true will catch every response instead of unregistering
     * the conversation after a response has been received and instead unregisters it
     * after the conversation-timeout value in the configuration has been reached.
     *
     * @return current instance of the Conversation Object
     */
    public Conversation<TRequest, TResponse> enableConcurrentResponseProcessing() {
        this.concurrentResponseProcessing = true;
        return this;
    }

    /**
     * Returns an empty conversation.
     *
     * @param <TRequest>  the type parameter
     * @param <TResponse> the type parameter
     * @return empty conversation object
     */
    @SuppressWarnings("unchecked")
    public static <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> empty() {
        return (Conversation<TRequest, TResponse>) EMPTY;
    }
}
