package net.sxlver.jrpc.core.protocol.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;

@Getter
@AllArgsConstructor
public class SyncRegisteredClientsPacket extends Packet {
    private @NonNull JRPCClientInformation[] registeredClients;
}