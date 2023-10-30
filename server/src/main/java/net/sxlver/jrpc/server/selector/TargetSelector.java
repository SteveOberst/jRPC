package net.sxlver.jrpc.server.selector;

import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.server.model.JRPCClientInstance;

import java.util.Collection;
import java.util.Collections;

public interface TargetSelector {
    Collection<JRPCClientInstance> select(final String target, final Collection<JRPCClientInstance> registeredClients);
}
