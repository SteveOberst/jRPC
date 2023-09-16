package net.sxlver.jrpc.core.protocol;

import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;

public interface Message {

    String source();

    String target();

    JRPCMessage.TargetType targetType();

    ConversationUID conversationId();

    byte[] data();

    enum TargetType {
        /**
         * Message is meant for a client of the given type and will be
         * distributed through a load balancer and then sent to a server
         * of the provided type. The target represents a server type.
         */
        LOAD_BALANCED,

        /**
         * Message is meant for a specific client. The given target should be
         * the server's unique identifier.
         */
        DIRECT,

        /**
         * Forwards the message to every client instance matching the given type.
         */
        ALL
    }
}
