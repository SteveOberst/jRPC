package net.sxlver.jrpc.client.protocol.processors;

import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.packet.KeepAlivePacket;

public class KeepAliveReceiver<T extends KeepAlivePacket> implements MessageHandler<T> {

    private final JRPCClient client;

    public KeepAliveReceiver(final @NonNull JRPCClient client) {
        this.client = client;
    }

    @Override
    public void onReceive(final @NonNull MessageContext<T> context) {
        // respond with a keep alive packet
        client.sendKeepAlive();
    }

    @Override
    public boolean shouldAccept(final @NonNull Packet packet) {
        return packet instanceof KeepAlivePacket;
    }
}
