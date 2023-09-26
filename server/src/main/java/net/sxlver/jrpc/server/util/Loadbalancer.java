package net.sxlver.jrpc.server.util;

import com.google.common.collect.Lists;
import net.sxlver.jrpc.server.model.JRPCClientInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

public class Loadbalancer {
    public static Collection<JRPCClientInstance> pick(final Collection<JRPCClientInstance> loadBalancedServers, final String type) {
        AtomicReference<Collection<JRPCClientInstance>> server = new AtomicReference<>();
        loadBalancedServers.stream()
                .filter(jrpcClientInstance -> jrpcClientInstance.getType().equals(type))
                .min(Comparator.comparingLong(JRPCClientInstance::getLastWrite))
                .ifPresentOrElse(res -> server.set(Lists.newArrayList(res)), () -> server.set(new ArrayList<>()));
        return server.get();
    }
}
