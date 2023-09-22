package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;

public interface RawDataReceiver {
    void onReceive(final @NonNull String source, final @NonNull String target, final @NonNull Message.TargetType targetType, final @NonNull ConversationUID conversationUID, final byte[] data);
}
