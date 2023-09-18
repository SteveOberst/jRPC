package net.sxlver.jrpc.bukkit.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;

public class MessageContext {
    private final String source;
    private final String target;
    private final Message.TargetType targetType;
    private final ConversationUID conversationUID;

    public MessageContext(final @NonNull String source, final @NonNull String target, final @NonNull Message.TargetType targetType, final @NonNull ConversationUID conversationUID) {
        this.source = source;
        this.target = target;
        this.targetType = targetType;
        this.conversationUID = conversationUID;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Message.TargetType getTargetType() {
        return targetType;
    }

    public ConversationUID getConversationUID() {
        return conversationUID;
    }
}
