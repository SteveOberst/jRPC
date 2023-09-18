package net.sxlver.jrpc.client.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;

@Getter
@AllArgsConstructor
public class MessageContext {
    private final @NonNull String source;
    private final @NonNull String target;
    private final @NonNull Message.TargetType targetType;
    private final @NonNull ConversationUID conversationUID;
}
