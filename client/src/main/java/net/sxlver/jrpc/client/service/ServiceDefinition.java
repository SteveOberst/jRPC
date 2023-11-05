package net.sxlver.jrpc.client.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.client.service.exception.ProcedureInvocationException;
import net.sxlver.jrpc.core.protocol.Errors;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationResponse;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public abstract class ServiceDefinition implements MessageHandler<Packet> {
    private Cache<Class<? extends Packet>, Procedure> procedures = CacheBuilder.newBuilder().build();

    private final JRPCClient client;

    public ServiceDefinition(final JRPCClient client) {
        this.client = client;
        registerProcedures();
    }

    @Override
    public void onReceive(final MessageContext<Packet> context) {
        final Packet request = context.getRequest();
        final Procedure procedure = getProcedure(request.getClass());
        if(procedure == null) {
            context.replyDirectly(new ErrorInformationResponse(Errors.ERR_INTERNAL_ERROR, String.format("No procedure for %s", request.getClass())));
            return;
        }

        final Function<Packet, Packet> invokeFunc = paramRequest -> {
            try {
                return procedure.invoke(paramRequest);
            } catch (final ProcedureInvocationException exception) {
                client.getLogger().fatal(exception);
                return new ErrorInformationResponse(Errors.ERR_INTERNAL_ERROR, exception.getMessage());
            }
        };

        if(procedure.isAsync()) {
            CompletableFuture.supplyAsync(() -> invokeFunc.apply(request)).thenAccept(packet -> {
                if(packet != null) context.replyDirectly(packet);
            });
        }else {
            final Packet response = invokeFunc.apply(request);
            if(response != null) {
                context.replyDirectly(response);
            }
        }

        client.getLogger().debugFinest("Invoked procedure {} in service {}", procedure.getMethod().getName(), getClass().getSimpleName());
    }

    private void registerProcedures() {
        for (Method method : getClass().getMethods()) {
            final ProcedureImplementation procedureAnnotation = method.getAnnotation(ProcedureImplementation.class);
            if(procedureAnnotation == null) continue;

            final ProcedureType procedureType = procedureAnnotation.type();
            if(!procedureType.checkPrerequisites(method)) {
                client.getLogger().warn("{} failed the pre-requisites check for type {}. Check the documentation on how to declare procedures.", method.getName(), procedureType);
                continue;
            }

            final Procedure procedure = procedureType.parse(this, method);
            procedures.put(procedure.getRequestType(), procedure);
            client.getLogger().debugFine("Registered procedure implementation {} in service {}", method.getName(), getClass().getSimpleName());
        }
    }

    @Override
    public boolean shouldAccept(@NonNull Packet packet) {
        return getProcedure(packet.getClass()) != null;
    }

    @Nullable
    public Procedure getProcedure(Class<? extends Packet> cls) {
        return procedures.getIfPresent(cls);
    }

    public JRPCClient getClient() {
        return client;
    }
}
