package net.sxlver.jrpc.client.service;

import net.sxlver.jrpc.core.protocol.Packet;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * Enumerates the types of procedures available for the RPC framework.
 */
public enum ProcedureTypes implements ProcedureType {
    /**
     * Represents a query-type procedure that retrieves data from
     * the remote server and returns a corresponding response.
     *
     * <p>
     * This procedure facilitates data retrieval and interaction
     * with the server's resources.
     */
    QUERY(method -> method.getParameterCount() == 1
            && Packet.class.isAssignableFrom(method.getParameterTypes()[0])
            && Packet.class.isAssignableFrom(method.getReturnType())) {
        @Override
        public @NotNull Procedure parse(ServiceDefinition service, Method method) {
            final ProcedureImplementation procedureAnnotation = method.getAnnotation(ProcedureImplementation.class);
            final @SuppressWarnings("All") Class<? extends Packet> requestType = (Class<? extends Packet>) method.getParameterTypes()[0];
            final @SuppressWarnings("All") Class<? extends Packet> responseType = (Class<? extends Packet>) method.getReturnType();
            return new Procedure(service, method, requestType, responseType, procedureAnnotation);
        }
    },

    /**
     * Represents a consumer-type procedure that accepts and processes
     * data received from the remote server.
     *
     * <p>
     * This procedure enables the client to send data and trigger
     * server-side actions without expecting a direct response.
     */
    CONSUMER(method -> method.getParameterCount() == 1 && Packet.class.isAssignableFrom(method.getParameterTypes()[0])) {
        @Override
        public @NotNull Procedure parse(ServiceDefinition service, Method method) {
            final ProcedureImplementation procedureAnnotation = method.getAnnotation(ProcedureImplementation.class);
            final @SuppressWarnings("All") Class<? extends Packet> requestType = (Class<? extends Packet>) method.getParameterTypes()[0];
            return new Procedure(service, method, requestType, null, procedureAnnotation);
        }
    };

    private final Predicate<Method> methodValidator;

    ProcedureTypes(final Predicate<Method> methodValidator) {
        this.methodValidator = methodValidator;
    }

    @Override
    public boolean checkPrerequisites(final Method method) {
        return methodValidator.test(method);
    }
}
