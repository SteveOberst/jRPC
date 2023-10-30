package net.sxlver.jrpc.core.protocol;

import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;

import java.util.Arrays;

public interface Message {

    int MAX_PACKET_LENGTH = 2097152;

    String source();

    String target();

    TargetType targetType();

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
        TYPE,

        /**
         * Forwards a message to every single client instance connected to the server.
         * The target for this operation does not matter.
         */
        ALL,

        /**
         * Message is only meant for the server and will not be redirected to any client.
         */
        SERVER;


        public static TargetType fromString(final String type) {
            return Arrays.stream(values()).filter(targetType -> targetType.toString().equalsIgnoreCase(type)).findFirst().orElse(null);
        }
    }
}
