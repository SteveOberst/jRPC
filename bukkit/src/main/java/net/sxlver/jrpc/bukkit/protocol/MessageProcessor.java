package net.sxlver.jrpc.bukkit.protocol;

import net.sxlver.jrpc.core.protocol.Packet;

public interface MessageProcessor {
    void onReceive(final MessageContext context, final Packet packet);

    default boolean shouldAccept(final Packet packet) {
        return true;
    }
}
