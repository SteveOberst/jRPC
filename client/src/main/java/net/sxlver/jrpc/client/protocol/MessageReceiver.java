package net.sxlver.jrpc.client.protocol;

import net.sxlver.jrpc.core.protocol.Packet;

public interface MessageReceiver<T extends Packet> {
    void onReceive(final T packet);
}
