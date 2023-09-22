package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;

/**
 * The interface Message receiver.
 */
public interface MessageReceiver {
    /**
     * Called when a message is being received.
     *
     * @param context the context
     * @param packet  the packet
     */
    void onReceive(final @NonNull MessageContext context, final @NonNull Packet packet);

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
