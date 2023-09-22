package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationPacket;
import net.sxlver.jrpc.core.util.TriConsumer;

import java.util.function.BiConsumer;

public abstract class ErrorHandler implements MessageReceiver {

    private final JRPCClient client;
    private TriConsumer<MessageReceiver, MessageContext, Throwable> handler;

    public ErrorHandler(final JRPCClient client, final TriConsumer<MessageReceiver, MessageContext, Throwable> handler) {
        this.client = client;
        this.handler = handler;
    }

    @Override
    public void onReceive(final @NonNull MessageContext context, final @NonNull Packet packet) {
        final ErrorInformationPacket errorPacket = (ErrorInformationPacket) packet;
        client.getLogger().warn("Received error message from the server. Error code: {}, Description: {}", errorPacket.getErrorCode(), errorPacket.getErrorDescription());
    }

    public void onException(final TriConsumer<MessageReceiver, MessageContext, Throwable> handler) {
        this.handler = handler;
    }

    @Override
    public boolean shouldAccept(final @NonNull Packet packet) {
        return packet instanceof ErrorInformationPacket;
    }


    void raiseException(final MessageReceiver receiver, final MessageContext context, final Throwable cause) {
        handler.accept(receiver, context, cause);
    }
}
