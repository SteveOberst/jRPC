package net.sxlver.jrpc.client.protocol;

import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.ErrorInformationHolder;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationResponse;
import net.sxlver.jrpc.core.util.TriConsumer;

public abstract class ErrorHandler<T extends ErrorInformationHolder> implements MessageHandler<T> {

    private final JRPCClient client;
    private TriConsumer<MessageHandler<?>, MessageContext<T>, Throwable> handler;

    public ErrorHandler(final JRPCClient client, final TriConsumer<MessageHandler<?>, MessageContext<T>, Throwable> handler) {
        this.client = client;
        this.handler = handler;
    }

    @Override
    public void onReceive(final @NonNull MessageContext<T> context) {
        final ErrorInformationResponse errorPacket = (ErrorInformationResponse) context.getRequest();
        client.getLogger().warn("Received error message from the server. Error code: {}, Description: {}", errorPacket.getErrorCode(), errorPacket.getErrorDescription());
    }

    public void onException(final TriConsumer<MessageHandler<?>, MessageContext<T>, Throwable> handler) {
        this.handler = handler;
    }

    @Override
    public boolean shouldAccept(final @NonNull Packet packet) {
        return packet instanceof ErrorInformationHolder;
    }


    void raiseException(final MessageHandler<?> messageHandler, final MessageContext<?> context, final Throwable cause) {
        handler.accept(messageHandler, (MessageContext<T>) context, cause);
    }
}
