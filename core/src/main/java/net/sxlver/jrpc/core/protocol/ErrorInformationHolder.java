package net.sxlver.jrpc.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public abstract class ErrorInformationHolder extends Packet {
    private @NonNull String errorDescription;
    private @Nullable Throwable cause;
}
