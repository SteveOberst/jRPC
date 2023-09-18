package net.sxlver.jrpc.client.protocol.processors;

import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.ErrorHandler;
import net.sxlver.jrpc.client.protocol.MessageContext;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.function.BiConsumer;

public class DefaultErrorHandler extends ErrorHandler {
    public DefaultErrorHandler(final JRPCClient client) {
        super(client, (context, throwable) -> client.getLogger().info("An unhandled exception occurred: {}", ExceptionUtils.getStackTrace(throwable)));
    }

    public DefaultErrorHandler(final JRPCClient client, final BiConsumer<MessageContext, Throwable> handler) {
        super(client, handler);
    }
}
