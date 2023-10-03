package net.sxlver.jrpc.client.protocol.processors;

import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.ErrorHandler;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.core.protocol.ErrorInformationHolder;
import net.sxlver.jrpc.core.util.TriConsumer;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class DefaultErrorHandler<T extends ErrorInformationHolder> extends ErrorHandler<T> {
    public DefaultErrorHandler(final JRPCClient client) {
        super(client, (receiver, context, throwable) -> client.getLogger().info("An unhandled exception occurred: {}", ExceptionUtils.getStackTrace(throwable)));
    }

    public DefaultErrorHandler(final JRPCClient client, final TriConsumer<MessageHandler<?>, MessageContext<T>, Throwable> handler) {
        super(client, handler);
    }
}
