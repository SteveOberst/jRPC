package net.sxlver.jrpc.server.protocol;

import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.server.protocol.impl.QueryClusterInformationHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum DefaultHandlerRegistry {
    QUERY_CLUSTER_INFORMATION(QueryClusterInformationHandler::new);

    private final Supplier<ServerMessageHandler<?>> handler;

    DefaultHandlerRegistry(final Supplier<ServerMessageHandler<?>> handler) {
        this.handler = handler;
    }

    public ServerMessageHandler<?> getHandler() {
        return handler.get();
    }

    private static ServerMessageHandler<Packet> cast(final ServerMessageHandler<?> toCast) {
        return (ServerMessageHandler<Packet>) toCast;
    }

    public static Collection<ServerMessageHandler<Packet>> getMessageHandlers() {
        return Arrays.stream(values()).map(DefaultHandlerRegistry::getHandler).map(DefaultHandlerRegistry::cast).collect(Collectors.toList());
    }
}
