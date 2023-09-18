package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageReceiver;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.packet.ErrorResponsePacket;

import java.util.function.BiConsumer;

public abstract class ErrorHandler implements MessageReceiver {

    private final JRPCClient client;
    private BiConsumer<MessageContext, Throwable> handler;

    public ErrorHandler(final JRPCClient client, final BiConsumer<MessageContext, Throwable> handler) {
        this.client = client;
        this.handler = handler;
    }

    @Override
    public void onReceive(final @NonNull MessageContext context, final @NonNull Packet packet) {
        final ErrorResponsePacket errorPacket = (ErrorResponsePacket) packet;
        client.getLogger().warn("Received error message from the server. Error code: {}, Description: {}", errorPacket.getErrorCode(), errorPacket.getErrorDescription());
    }

    public void onException(final BiConsumer<MessageContext, Throwable> handler) {
        this.handler = handler;
    }

    @Override
    public boolean shouldAccept(final @NonNull Packet packet) {
        return packet instanceof ErrorResponsePacket;
    }

    void raiseException(final MessageContext context, final Throwable cause) {
        handler.accept(context, cause);
    }
}
