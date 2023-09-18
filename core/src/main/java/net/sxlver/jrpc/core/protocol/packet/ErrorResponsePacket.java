package net.sxlver.jrpc.core.protocol.packet;

import net.sxlver.jrpc.core.protocol.Packet;

public class ErrorResponsePacket extends Packet {
    private final int errorCode;
    private final String errorDescription;

    public ErrorResponsePacket(int errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
