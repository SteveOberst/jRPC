package net.sxlver.jrpc.core.protocol.packet;

import net.sxlver.jrpc.core.protocol.Packet;

public class HandshakeStatusPacket extends Packet {
    private boolean status;
    private String errorMessage = "";

    public HandshakeStatusPacket(final boolean status) {
        this.status = status;
    }

    public HandshakeStatusPacket(final boolean status, final String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public boolean getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
