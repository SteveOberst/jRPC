package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores stateful information about the incoming data being handled
 *
 * @param <T> the type parameter
 */
public class MessageContext<T extends Packet> {

    private final JRPCClient client;

    private final String source;
    private final String target;
    private final Message.TargetType targetType;
    private final ConversationUID conversationUID;

    private final T request;
    private final @Nullable Packet response;

    private boolean responseContext;

    /**
     * Instantiates a new Message context.
     *
     * <p>This class can represent two different contexts, either one in which
     *    a response is being handled or one in which a request is being handled.
     *    {@link #response} may be null accordingly.
     *
     * @param client          the client
     * @param request         the request
     * @param response        the response or null
     * @param source          the source
     * @param target          the target
     * @param targetType      the target type
     * @param conversationUID the conversation uid
     */
    public MessageContext(final @NonNull JRPCClient client,
                          final @NonNull T request,
                          final @Nullable Packet response,
                          final @NonNull String source,
                          final @NonNull String target,
                          final @NonNull Message.TargetType targetType,
                          final @NonNull ConversationUID conversationUID) {

        this.client = client;
        this.request = request;
        this.response = response;
        this.source = source;
        this.target = target;
        this.targetType = targetType;
        this.conversationUID = conversationUID;
        this.responseContext = response != null;
    }


    /**
     * Reply to the incoming request, usually used in a Request-Response model.
     *
     * @param <TRequest> the type parameter
     * @param response   the response to send to the source
     */
    public <TRequest extends Packet> void replyDirectly(final @NonNull TRequest response) {
        replyDirectly(response, null);
    }

    /**
     * Reply to the request, keeping the conversation "alive", awaiting the next response from the other end.
     *
     * @param <TRequest>       the type parameter
     * @param <TResponse>      the type parameter
     * @param message          the message to send to the source
     * @param expectedResponse the expected response
     * @return conversation object representing the new conversation between this instance and the source
     */
    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> replyDirectly(final @NonNull TRequest message, final @Nullable Class<TResponse> expectedResponse) {
        return client.publish(message, new MessageTarget(Message.TargetType.DIRECT, source), expectedResponse, conversationUID);
    }

    /**
     * Returns the {@link JRPCClient} instance.
     *
     * @return the client
     */
    public JRPCClient getClient() {
        return client;
    }

    /**
     * The request being handled or that was previously sent if this object represents
     * a response handling context
     *
     * @return the request
     */
    public T getRequest() {
        return request;
    }

    /**
     * The response being handled or null if this object represents a request handling context
     *
     * @param <TResponse> the type parameter
     * @return the response
     */
    @SuppressWarnings("unchecked")
    public <TResponse extends Packet> TResponse getResponse() {
        return (TResponse) response;
    }

    /**
     * The client the message originates from
     *
     * @return the source
     */
    @NotNull
    public String getSource() {
        return source;
    }

    /**
     * Gets target.
     *
     * @return the target
     */
    @NotNull
    public String getTarget() {
        return target;
    }

    /**
     * Gets target type.
     *
     * @return the target type
     */
    @NotNull
    public Message.TargetType getTargetType() {
        return targetType;
    }

    /**
     * The conversation uid for the current context.
     *
     * @return the conversation uid
     */
    @NotNull
    public ConversationUID getConversationUID() {
        return conversationUID;
    }

    /**
     * @return whether this object represents a request or response handling context
     */
    public boolean isResponseContext() {
        return responseContext;
    }
}
