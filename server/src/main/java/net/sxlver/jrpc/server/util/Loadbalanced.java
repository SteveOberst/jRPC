package net.sxlver.jrpc.server.util;

import net.sxlver.jrpc.server.model.JRPCClientInstance;

import java.util.Collection;
import java.util.Comparator;

public class Loadbalanced {
    public static JRPCClientInstance pick(final Collection<JRPCClientInstance> loadBalancedServers) {
        return loadBalancedServers.stream()
                .min(Comparator.comparingLong(JRPCClientInstance::getLastWrite))
                .orElse(null);
    }
}
