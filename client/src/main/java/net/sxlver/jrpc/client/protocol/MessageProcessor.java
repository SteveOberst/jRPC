package net.sxlver.jrpc.client.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.processors.DefaultErrorHandler;
import net.sxlver.jrpc.client.protocol.processors.KeepAliveReceiver;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.ErrorInformationHolder;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
import net.sxlver.jrpc.core.util.TriConsumer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class MessageProcessor implements RawDataReceiver {

    private final Cache<Class<? extends MessageHandler<?>>, MessageHandler<?>> registeredMessageReceivers = CacheBuilder.newBuilder().build();
    private final JRPCClient client;

    private ErrorHandler<? extends ErrorInformationHolder> errorHandler;
    private TriConsumer<MessageHandler<Packet>, MessageContext<Packet>, Throwable> internalErrorHandler;

    public MessageProcessor(final @NonNull JRPCClient client) {
        this.client = client;
        populateDefaultHandlers();
    }

    @Override
    @SuppressWarnings("Unchecked")
    public void onReceive(final @NonNull String source,
                          final @NonNull String target,
                          final @NonNull Message.TargetType targetType,
                          final @NonNull ConversationUID conversationUID,
                          final byte[] data) {
        final JRPCClientChannelHandler netHandler = client.getNetHandler();
        final Packet packet = PacketDataSerializer.deserializePacket(data);
        final MessageContext<Packet> context = new MessageContext<>(client, packet, source, target, targetType, conversationUID);

        if(netHandler.isObserverPresent(conversationUID)) {
            final Conversation<Packet, Packet> conversation = netHandler.getObserver(conversationUID);
            if(!conversation.isConcurrentResponseProcessing()) {
                // we don't expect more than one response, so we can unregister the conversation at this point
                netHandler.invalidateConversation(conversationUID);
            }

            // Have we received an error from the other end?
            if(packet instanceof ErrorInformationHolder errorPacket) {
                // If the provided cause is null we just assign a new instance of Exception
                final Throwable t = errorPacket.getCause() != null ? errorPacket.getCause() : new Exception();
                // call the error handler
                conversation.except(t, errorPacket);
                return;
            }

            if(packet.getClass() != conversation.getExpectedResponse()) {
                // notify handler of the received packet
                conversation.onResponse(packet);
                // do not continue if overrideHandlers is true on the conversation
                if(conversation.shouldOverrideHandlers()) {
                    return;
                }
            }else {
                client.getLogger().warn("Received invalid response. [Expected: {}] [Received: {}]", conversation.getExpectedResponse(), packet.getClass());
            }
        }

        for (final Map.Entry<Class<? extends MessageHandler<?>>, MessageHandler<?>> entry : registeredMessageReceivers.asMap().entrySet()) {
            final MessageHandler<Packet> handler = (MessageHandler<Packet>) entry.getValue();
            try {
                if(!handler.shouldAccept(packet)) continue;
                handler.onReceive(context);
            }catch(final Exception exception) {
                internalErrorHandler.accept(handler, context, exception);
            }
        }
    }

    public <T extends ErrorInformationHolder> MessageProcessor setErrorHandler(final @NonNull TriConsumer<MessageHandler<T>, MessageContext<T>, Throwable> handler) {
        return setErrorHandler(new DefaultErrorHandler<>(client, handler));
    }

    public <T extends ErrorInformationHolder> MessageProcessor setErrorHandler(final @NonNull ErrorHandler<T> errorHandler) {
        registerHandler(errorHandler);
        this.errorHandler = errorHandler;
        return this;
    }

    public MessageProcessor setInternalErrorHandler(TriConsumer<MessageHandler<Packet>, MessageContext<Packet>, Throwable> handler) {
        this.internalErrorHandler = handler;
        return this;
    }

    private void invalidateErrorHandler() {
        unregisterHandler(this.errorHandler.getClass());
        this.errorHandler = null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> MessageProcessor registerHandler(final @NonNull MessageHandler<T> handler) {
        registeredMessageReceivers.put((Class<? extends MessageHandler<?>>) handler.getClass(), handler);
        client.getLogger().debug("Registered message receiver {}", handler.getClass());
        return this;
    }

    public <T extends MessageHandler<?>> MessageProcessor unregisterHandler(final @NonNull Class<T> handlerCls) {
        registeredMessageReceivers.invalidate(handlerCls);
        client.getLogger().debug("Unregistered message receiver {}", handlerCls);
        return this;
    }

    private void populateDefaultHandlers() {
        registerHandler(new KeepAliveReceiver<>(client));
        setErrorHandler(new DefaultErrorHandler<>(client, (messageHandler, messageContext, throwable) -> {
            client.getLogger().warn("{} received error message from the other end. [Source: {}] {}", messageContext.getSource(), throwable.getMessage());
            client.getLogger().debug("Exception thrown: {}", ExceptionUtils.getStackTrace(throwable));
        }));

        this.internalErrorHandler = (handler, messageContext, throwable) -> {
            client.getLogger().warn("Class {} has encountered an error whilst processing a request from '{}'. [Packet Type: {}]",
                    handler.getClass(), messageContext.getSource(), throwable.getMessage()
            );
            client.getLogger().fatal(ExceptionUtils.getStackTrace(throwable));
        };
    }
}
