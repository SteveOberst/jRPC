package net.sxlver.jrpc.client.service;

import net.sxlver.jrpc.client.service.exception.ProcedureInvocationException;
import net.sxlver.jrpc.core.protocol.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class Procedure {
    private final ServiceDefinition service;

    private final Method method;
    private final Class<? extends Packet> requestType;
    private final Class<? extends Packet> responseType;
    private final ProcedureType type;
    private final boolean async;

    public Procedure(@NotNull  final ServiceDefinition service,
                     @NotNull  final Method method,
                     @NotNull final Class<? extends Packet> requestType,
                     @Nullable final Class<? extends Packet> responseType,
                     @NotNull  final ProcedureImplementation metadata) {

        this.service = service;
        this.method = method;
        this.requestType = requestType;
        this.responseType = responseType;
        this.type = metadata.type();
        this.async = metadata.async();
    }

    public Packet invoke(Packet request) throws ProcedureInvocationException {
        try {
            method.setAccessible(true);
            final Object response = method.invoke(service, request);
            if(response == null) return null;
            return (Packet) response;
        } catch (Exception exception) {
            String errorMsg = exception.getClass() + " whilst invoking " + service.getClass().getSimpleName() + "#" + method.getName();
            throw new ProcedureInvocationException(errorMsg, exception);
        }
    }

    private static boolean checkPrerequisites(final Method method) {
        return method.getParameterCount() == 1 && Packet.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    public Method getMethod() {
        return method;
    }

    public Class<? extends Packet> getRequestType() {
        return requestType;
    }

    public Class<? extends Packet> getResponseType() {
        return responseType;
    }

    public ProcedureType getType() {
        return type;
    }

    public boolean isAsync() {
        return async;
    }
}
