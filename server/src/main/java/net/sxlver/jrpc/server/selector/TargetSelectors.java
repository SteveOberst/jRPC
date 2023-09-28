package net.sxlver.jrpc.server.selector;

import com.google.common.collect.Lists;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.server.model.JRPCClientInstance;
import net.sxlver.jrpc.server.util.Loadbalancer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;

public enum TargetSelectors implements TargetSelector {
    TARGET_SELECTOR_ALL(Message.TargetType.TYPE, (target, jrpcClientInstances) -> {
        return jrpcClientInstances.stream().filter(jrpcClientInstance -> jrpcClientInstance.getType().equals(target)).toList();
    }),
    TARGET_SELECTOR_LOAD_BALANCED(Message.TargetType.LOAD_BALANCED, (target, jrpcClientInstances) -> {
        return Lists.newArrayList(Loadbalancer.pick(jrpcClientInstances, target));
    }),
    TARGET_SELECTOR_BROADCAST(Message.TargetType.ALL, (target, jrpcClientInstances) -> new ArrayList<>(jrpcClientInstances)),
    TARGET_SELECTOR_DIRECT(Message.TargetType.DIRECT, (target, jrpcClientInstances) -> {
        return jrpcClientInstances.stream().filter(jrpcClientInstance -> jrpcClientInstance.getUniqueId().equals(target)).toList();
    });

    private final Message.TargetType type;
    private final BiFunction<String, Collection<JRPCClientInstance>, Collection<JRPCClientInstance>> filterFunc;

    TargetSelectors(final Message.TargetType type, final BiFunction<String, Collection<JRPCClientInstance>, Collection<JRPCClientInstance>> filterFunc) {
        this.type = type;
        this.filterFunc = filterFunc;
    }

    public Message.TargetType getTarget() {
        return type;
    }

    @Override
    public Collection<JRPCClientInstance> select(String target, Collection<JRPCClientInstance> registeredClients) {
        return filterFunc.apply(target, registeredClients);
    }

    public static TargetSelector getByTargetType(final Message.TargetType type) {
        return Arrays.stream(values()).filter(selector -> selector.type == type).findFirst().orElseThrow(NullPointerException::new);
    }
}
