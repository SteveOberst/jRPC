package net.sxlver.jrpc.core.serialization.exception;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message) {
        super(message);
    }
    public DeserializationException(Throwable cause) {
        super(cause);
    }
}
