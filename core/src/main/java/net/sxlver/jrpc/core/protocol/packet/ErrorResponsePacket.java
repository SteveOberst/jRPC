package net.sxlver.jrpc.core.protocol.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;

@Getter
@AllArgsConstructor
public class ErrorResponsePacket extends Packet {
    private @NonNull int errorCode;
    private @NonNull String errorDescription;
}
