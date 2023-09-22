package net.sxlver.jrpc.client.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageContext<T extends Packet> {

    private final JRPCClient client;

    private final String source;
    private final String target;
    private final Message.TargetType targetType;
    private final ConversationUID conversationUID;

    private final T request;
    private @Nullable Packet response;

    public MessageContext(final @NonNull JRPCClient client,
                          final @NonNull T request,
                          final @NonNull String source,
                          final @NonNull String target,
                          final @NonNull Message.TargetType targetType,
                          final @NonNull ConversationUID conversationUID) {

        this.client = client;
        this.request = request;
        this.source = source;
        this.target = target;
        this.targetType = targetType;
        this.conversationUID = conversationUID;
    }


    public <TRequest extends Packet> void respond(final @NonNull TRequest response) {
        respond(response, null);
    }

    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> respond(final @NonNull TRequest message, final @Nullable Class<TResponse> expectedResponse) {
        return client.write(message, new MessageTarget(targetType, source), expectedResponse, null);
    }

    public JRPCClient getClient() {
        return client;
    }

    public T getRequest() {
        return request;
    }

    @SuppressWarnings("unchecked")
    public <TResponse extends Packet> TResponse getResponse() {
        return (TResponse) response;
    }

    @NotNull
    public String getSource() {
        return source;
    }

    @NotNull
    public String getTarget() {
        return target;
    }

    @NotNull
    public Message.TargetType getTargetType() {
        return targetType;
    }

    @NotNull
    public ConversationUID getConversationUID() {
        return conversationUID;
    }
}
