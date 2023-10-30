package net.sxlver.jrpc.client.service;

import net.sxlver.jrpc.client.service.exception.ProcedureInvocationException;
import net.sxlver.jrpc.core.protocol.Packet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class Procedure {
    private final ServiceDefinition service;

    private final Method method;
    private final Class<? extends Packet> requestType;
    private final Class<? extends Packet> responseType;
    private final boolean async;

    public Procedure(final ServiceDefinition service,
                     final Method method,
                     final Class<? extends Packet> requestType,
                     final Class<? extends Packet> responseType,
                     final ProcedureImplementation metadata) {

        this.service = service;
        this.method = method;
        this.requestType = requestType;
        this.responseType = responseType;
        this.async = metadata.async();
    }

    public Packet invoke(Packet request) throws ProcedureInvocationException {
        try {
            method.setAccessible(true);
            return (Packet) method.invoke(service, request);
        } catch (Exception exception) {
            String errorMsg = exception.getClass() + " whilst invoking " + service.getClass().getSimpleName() + "#" + method.getName();
            throw new ProcedureInvocationException(errorMsg, exception);
        }
    }

    private static boolean checkPrerequisites(final Method method) {
        return method.getParameterCount() == 1 && Packet.class.isAssignableFrom(method.getParameterTypes()[0])
                && Packet.class.isAssignableFrom(method.getReturnType());
    }

    public static Procedure of(final ServiceDefinition service, final Method method) {
        if(!checkPrerequisites(method)) {
            throw new IllegalArgumentException(String.format("%s is not a valid procedure implementation.", method.getName()));
        }

        final ProcedureImplementation procedureAnnotation = method.getAnnotation(ProcedureImplementation.class);
        final @SuppressWarnings("All") Class<? extends Packet> requestType = (Class<? extends Packet>) method.getParameterTypes()[0];
        final @SuppressWarnings("All") Class<? extends Packet> responseType = (Class<? extends Packet>) method.getReturnType();
        return new Procedure(service, method, requestType, responseType, procedureAnnotation);
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

    public boolean isAsync() {
        return async;
    }
}
