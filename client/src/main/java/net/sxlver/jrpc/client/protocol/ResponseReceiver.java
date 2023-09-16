package net.sxlver.jrpc.client.protocol;

import net.sxlver.jrpc.core.protocol.Packet;

public interface ResponseReceiver<T extends Packet> {
    void onReceive(final Packet packet);
}
