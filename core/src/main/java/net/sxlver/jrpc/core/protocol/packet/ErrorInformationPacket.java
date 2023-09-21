package net.sxlver.jrpc.core.protocol.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ErrorInformationHolder;
import net.sxlver.jrpc.core.protocol.Packet;
import org.jetbrains.annotations.Nullable;

@Getter
public class ErrorInformationPacket extends ErrorInformationHolder {
    private int errorCode;

    public ErrorInformationPacket(final int errorCode, final @NonNull String errorDescription, final @Nullable Throwable cause) {
        super(errorDescription, cause);
        this.errorCode = errorCode;
    }
}
