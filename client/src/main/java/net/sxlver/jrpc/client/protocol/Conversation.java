package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.ErrorInformationHolder;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationPacket;
import net.sxlver.jrpc.core.util.TimedCache;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;


/**
 * The type Conversation.
 *
 * @param <TRequest>  the type parameter
 * @param <TResponse> the type parameter
 */
public final class Conversation<TRequest extends Packet, TResponse extends Packet> implements TimedCache.NotifyOnExpire {

    private final TRequest request;
    private final ConversationUID conversationUID;
    private Class<? extends Packet> expectedResponse;

    private BiConsumer<TRequest, MessageContext<TResponse>> responseConsumer = (req, res) -> {};
    private BiConsumer<Throwable, ErrorInformationHolder> errorHandler = (throwable, errorInformationPacket) -> {};
    private BiConsumer<TRequest, Set<MessageContext<TResponse>>> timeoutHandler = (tRequest, messageContexts) -> {};
    private boolean overrideHandlers;
    private boolean concurrentResponseProcessing;

    private static final Conversation<?, ?> EMPTY = new Conversation<>();

    private boolean alwaysNotifyTimeout;
    private volatile boolean handlerCalled;

    private Set<MessageContext<TResponse>> responses = Collections.newSetFromMap(new ConcurrentHashMap<>());

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
    void onResponse(final MessageContext<TResponse> response) {
        this.handlerCalled = true;
        this.responses.add(response);
        responseConsumer.accept(request, response);
    }

    /**
     * Action to be run on the response when one was received.
     *
     * @param consumer the consumer
     * @return the conversation
     */
    public Conversation<TRequest, TResponse> onResponse(final @NonNull BiConsumer<TRequest, MessageContext<TResponse>> consumer) {
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
     * Settings this option to true will invoke {@link #onTimeout()} when the Conversation
     * expired, regardless of whether the handler has been called already.
     *
     * @return current instance of the Conversation Object
     */
    public Conversation<TRequest, TResponse> alwaysNotifyTimeout() {
        this.alwaysNotifyTimeout = true;
        return this;
    }

    /**
     * Called when the Conversation is marked as expired and removed from the cache.
     *
     * <p>The time span before Conversations are marked as expired depends on the
     * conversation-time-out value set in the configuration file. if this threshold
     * is reached and no response has been received the timeoutHandler will be called.
     *
     * <p>If {@link #alwaysNotifyTimeout()} has been set, the handler will be called
     * regardless of whether a response has been received after the conversation has
     * been marked as expired.
     *
     * @param timeoutHandler the handler that should be notified on timeout
     * @return current instance of the Conversation Object
     */
    public Conversation<TRequest, TResponse> onTimeout(final BiConsumer<TRequest, Set<MessageContext<TResponse>>> timeoutHandler) {
        this.timeoutHandler = timeoutHandler;
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

    @Override
    @ApiStatus.Internal
    public void notifyExpired() {
        if(!handlerCalled || alwaysNotifyTimeout) {
            expire();
        }
    }

    void expire() {
        timeoutHandler.accept(request, responses);
    }
}
