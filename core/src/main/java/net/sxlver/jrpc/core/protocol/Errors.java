package net.sxlver.jrpc.core.protocol;

import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.function.Function;

public class Errors {

    public static Errors ERR_UNKNOWN_ERR = new Errors(0x0);
    public static Errors ERR_NOT_AUTHENTICATED = new Errors(0x1);
    public static Errors ERR_INTERNAL_ERROR = new Errors(0x2);
    public static Errors ERR_NO_TARGET_FOUND = new Errors(0x3);
    public static Errors ERR_SELF_REFERENCE = new Errors(0x4);

    private int errorCode;
    private transient final Function<String, Throwable> throwableSupplier;

    private Errors(final int errorCode) {
        this.errorCode = errorCode;
        this.throwableSupplier = GenericError::new;
    }

    private Errors(final int errorCode, final @NonNull Function<String, Throwable> throwableSupplier) {
        this.errorCode = errorCode;
        this.throwableSupplier = throwableSupplier;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Throwable getCause(final String description) {
        return throwableSupplier.apply(description);
    }

    public static Errors fromCodeNoExcept(final int code) {
        try {
            return fromCode(code);
        }catch (final Exception e) {
            return ERR_UNKNOWN_ERR;
        }
    }

    public static Errors fromCode(final int code) throws IllegalAccessException {
        for (Field field : Errors.class.getFields()) {
            final Object o = field.get(Errors.class);
            if (!(o instanceof Errors error)) continue;
            if(error.getErrorCode() == code)
                return error;
        }
        return ERR_UNKNOWN_ERR;
    }

    public static class GenericError extends Exception {
        public GenericError(String message) {
            super(message);
        }
    }
}
