package net.sxlver.jrpc.core.protocol.packet;

import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;

public class SyncRegisteredClientsPacket extends Packet {
    private JRPCClientInformation[] registeredClients;

    public SyncRegisteredClientsPacket(final JRPCClientInformation[] registeredClients) {
        this.registeredClients = registeredClients;
    }

    public JRPCClientInformation[] getRegisteredClients() {
        return registeredClients;
    }
}
