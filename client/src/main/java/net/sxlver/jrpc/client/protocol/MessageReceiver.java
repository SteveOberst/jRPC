package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;

public interface MessageReceiver {
    void onReceive(final @NonNull MessageContext context, final @NonNull Packet packet);

    default boolean shouldAccept(final @NonNull Packet packet) {
        return true;
    }
}
