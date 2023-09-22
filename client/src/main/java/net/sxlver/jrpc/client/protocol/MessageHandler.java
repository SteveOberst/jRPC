package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;

/**
 * The interface Message handler.
 */
public interface MessageHandler<T extends Packet> {
    /**
     * Called when a message is being received.
     *
     * @param context the context
     */
    void onReceive(final @NonNull MessageContext<T> context);

    /**
     * Whether the packet type should be accepted or not.
     *
     * @param packet the packet
     * @return the boolean
     */
    default boolean shouldAccept(final @NonNull Packet packet) {
        return true;
    }
}
