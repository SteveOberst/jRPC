package net.sxlver.jrpc.core.protocol.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;

@Getter
@AllArgsConstructor
public class HandshakeStatusPacket extends Packet {
    private boolean success;
    private @NonNull String errorMessage;

    public HandshakeStatusPacket(final boolean success) {
        this.success = success;
        this.errorMessage = "";
    }
}
