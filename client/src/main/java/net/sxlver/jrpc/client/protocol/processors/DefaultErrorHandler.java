package net.sxlver.jrpc.client.protocol.processors;

import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.ErrorHandler;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageReceiver;
import net.sxlver.jrpc.core.util.TriConsumer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.function.BiConsumer;

public class DefaultErrorHandler extends ErrorHandler {
    public DefaultErrorHandler(final JRPCClient client) {
        super(client, (receiver, context, throwable) -> client.getLogger().info("An unhandled exception occurred: {}", ExceptionUtils.getStackTrace(throwable)));
    }

    public DefaultErrorHandler(final JRPCClient client, final TriConsumer<MessageReceiver, MessageContext, Throwable> handler) {
        super(client, handler);
    }
}
