package net.sxlver.jrpc.client.service.exception;

public class ProcedureInvocationException extends Exception {
    public ProcedureInvocationException(String message) {
        super(message);
    }

    public ProcedureInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
