package net.sxlver.jrpc.core.protocol.packet;

import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.ErrorInformationHolder;
import net.sxlver.jrpc.core.protocol.Errors;

@Getter
public class ErrorInformationPacket extends ErrorInformationHolder {
    private int errorCode;
    private transient Errors error;

    public ErrorInformationPacket(final Errors error, final @NonNull String errorDescription) {
        super(errorDescription);
        this.error = error;
        this.errorCode = error.getErrorCode();
    }

    @Override
    public Throwable getCause() {
        return getError().getCause(getErrorDescription());
    }

    public Errors getError() {
        if(error == null) {
            error = Errors.fromCodeNoExcept(errorCode);
        }
        return error;
    }
}
