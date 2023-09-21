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
 * @param <Request>  the type parameter
 * @param <Response> the type parameter
 */
public class Conversation<Request extends Packet, Response extends Packet> {

    private final Request request;
    private final ConversationUID conversationUID;
    private Class<? extends Packet> expectedResponse;

    private BiConsumer<Request, Response> responseConsumer = (req, res) -> {};
    private BiConsumer<Throwable, ErrorInformationHolder> errorHandler = (throwable, errorInformationPacket) -> {};
    private boolean overrideHandlers;
    private boolean acceptMultiple;

    private final static Conversation<?, ?> EMPTY = new Conversation<>();

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
    public Conversation(final @NonNull Request request, final @NonNull ConversationUID conversationUID, final @NonNull Class<? extends Packet> expectedResponse) {
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
    void onResponse(final Response response) {
        responseConsumer.accept(request, response);
    }

    /**
     * Action to be run on the response when one was received.
     *
     * @param consumer the consumer
     * @return the conversation
     */
    public Conversation<Request, Response> onResponse(final @NonNull BiConsumer<Request, Response> consumer) {
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
    public Conversation<Request, Response> overrideHandlers() {
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
     * @param errorHandler action to be run when an error response is received
     * @return current instance of the Conversation Object
     */
    @SuppressWarnings("unchecked")
    public <T extends ErrorInformationHolder> Conversation<Request, Response> onExcept(final @NonNull BiConsumer<Throwable, T> errorHandler) {
        this.errorHandler = (BiConsumer<Throwable, ErrorInformationHolder>) errorHandler;
        return this;
    }

    /**
     * Returns an empty conversation.
     *
     * @param <Request>  the type parameter
     * @param <Response> the type parameter
     * @return empty conversation object
     */
    @SuppressWarnings("unchecked")
    public static <Request extends Packet, Response extends Packet> Conversation<Request, Response> empty() {
        return (Conversation<Request, Response>) EMPTY;
    }
}
