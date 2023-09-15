package net.sxlver.jrpc.client.protocol;

import net.sxlver.jrpc.core.protocol.Packet;

public interface DataProcessor {
    void onReceive(final Packet packet);
}
