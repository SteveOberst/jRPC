package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Message;

public interface JRPCResponseReceiver<T extends Message> {
    void onReceive(final @NonNull T response);
}
