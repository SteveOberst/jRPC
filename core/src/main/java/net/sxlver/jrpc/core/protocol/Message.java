package net.sxlver.jrpc.core.protocol;

import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;

public interface Message {

    String getSource();

    String getTarget();

    JRPCMessage.TargetType getTargetType();

    byte[] getData();

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
        DIRECT
    }
}
