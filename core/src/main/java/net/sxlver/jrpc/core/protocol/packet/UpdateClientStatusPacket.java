package net.sxlver.jrpc.core.protocol.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;

@Getter
@AllArgsConstructor
public class UpdateClientStatusPacket extends Packet {

    private @NonNull Operation operation;
    private @NonNull JRPCClientInformation client;

    public enum Operation {
        REGISTER,
        UNREGISTER
    }
}
