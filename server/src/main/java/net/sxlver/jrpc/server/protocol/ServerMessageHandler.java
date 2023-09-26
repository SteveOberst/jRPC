package net.sxlver.jrpc.server.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.util.TriConsumer;
import net.sxlver.jrpc.server.JRPCServer;

import java.awt.im.spi.InputMethod;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class ServerMessageHandler<T extends Packet> {
    private final Class<T> target;
    private final BiConsumer<JRPCServer, ServerMessageContext<T>> handler;

    public ServerMessageHandler(Class<T> target, BiConsumer<JRPCServer, ServerMessageContext<T>> handler) {
        this.target = target;
        this.handler = handler;
    }

    public void handle(final @NonNull JRPCServer server, final @NonNull ServerMessageContext<T> context) {
        handler.accept(server, context);
    }

    public Class<T> getTarget() {
        return target;
    }

    public BiConsumer<JRPCServer, ServerMessageContext<T>> getHandler() {
        return handler;
    }
}
