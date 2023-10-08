package net.sxlver.jrpc.client.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.processors.DefaultErrorHandler;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.ErrorInformationHolder;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
import net.sxlver.jrpc.core.util.TriConsumer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

/**
 * A Default implementation for a {@link RawDataReceiver} that handles and parses incoming data
 * and passes it down to handlers that can be registered in this class
 */
@SuppressWarnings("UnstableApiUsage")
public class DefaultMessageProcessor implements RawDataReceiver {

    private final Cache<Class<? extends MessageHandler<?>>, MessageHandler<?>> registeredMessageReceivers = CacheBuilder.newBuilder().build();
    private final JRPCClient client;

    private ErrorHandler<? extends ErrorInformationHolder> errorHandler;

    /**
     * Instantiates a new DefaultMessageProcessor.
     *
     * @param client the client
     */
    public DefaultMessageProcessor(final @NonNull JRPCClient client) {
        this.client = client;
        populateDefaultHandlers();
    }

    /**
     * Receives incoming data, deserializes it back into it's java representation and passes it down to the handlers
     *
     * @param source          unique id of the client the messages originates from
     * @param target          the target of the message
     * @param targetType      the target type of the message
     * @param conversationUID the current conversation's id
     * @param data            the data received
     */
    @Override
    @SuppressWarnings("Unchecked")
    public void onReceive(final @NonNull String source,
                          final @NonNull String target,
                          final @NonNull Message.TargetType targetType,
                          final @NonNull ConversationUID conversationUID,
                          final byte[] data) {

        final JRPCClientChannelHandler netHandler = client.getNetHandler();
        final Packet packet = PacketDataSerializer.deserializePacket(data);
        if(packet == null) {
            client.getLogger().warn("Error whilst deserializing data from {} [Data Length: {}]", source, data.length);
            return;
        }

        client.getLogger().debugFiner("Received {} packet from {} [Conversation ID: {}] [Target: {}] [Target Type: {}]", packet.getClass(), source, conversationUID, target, targetType);
        final MessageContext<Packet> context = new MessageContext<>(client, packet, null, source, target, targetType, conversationUID);
        if(netHandler.isObserverPresent(conversationUID) && (!source.equals(client.getSource()) || netHandler.getObserver(conversationUID).getExpectedResponse() == packet.getClass())) {

            final Conversation<Packet, Packet> conversation = netHandler.getObserver(conversationUID);

            @SuppressWarnings("all")
            final MessageContext<Packet> conversationContext = new MessageContext<>(client, conversation.getRequest(), packet, source, target, targetType, conversationUID);
            if(!conversation.isConcurrentResponseProcessing()) {
                // we don't expect more than one response, so we can unregister the conversation at this point
                netHandler.invalidateConversation(conversationUID);
            }

            // Have we received an error from the other end?
            if(packet instanceof ErrorInformationHolder errorPacket) {
                // If the provided cause is null, which should usually not be the case we just assign a new instance of Exception
                final Throwable t = errorPacket.getCause() != null ? errorPacket.getCause() : new Exception();
                conversation.except(t, errorPacket);
                return;
            }

            if(packet.getClass() == conversation.getExpectedResponse()) {
                conversation.onResponse(conversationContext);
                client.getLogger().debugFinest("Invoking Conversation#onResponse for conversation id {}", conversationUID);
                // If the overrideHandlers flag has been set to true, we do not want to continue
                // and instead break here and skip calling the handlers.
                if(conversation.shouldOverrideHandlers()) {
                    client.getLogger().debugFinest("Overriding handlers for conversation id {}", conversationUID);
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
                errorHandler.raiseException(handler, context, exception);
            }
        }
    }

    /**
     * Override the default error handler.
     *
     * @param <T>     the type parameter
     * @param handler the handler
     * @return the current instance of this class
     */
    public <T extends ErrorInformationHolder> DefaultMessageProcessor
    setErrorHandler(final @NonNull TriConsumer<MessageHandler<?>, MessageContext<T>, Throwable> handler) {
        return setErrorHandler(new DefaultErrorHandler<>(client, handler));
    }

    /**
     * Override the default error handler.
     *
     * @param <T>          the type parameter
     * @param errorHandler the error handler
     * @return the current instance of this class
     */
    public <T extends ErrorInformationHolder>
    DefaultMessageProcessor setErrorHandler(final @NonNull ErrorHandler<T> errorHandler) {
        registerHandler(errorHandler);
        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * Registers a handler for incoming messages
     *
     * @param <T>     the type parameter
     * @param handler the handler
     * @return the current instance of this class
     */
    @SuppressWarnings("unchecked")
    public <T extends Packet>
    DefaultMessageProcessor registerHandler(final @NonNull MessageHandler<T> handler) {
        registeredMessageReceivers.put((Class<? extends MessageHandler<?>>) handler.getClass(), handler);
        client.getLogger().debugFine("Registered message receiver {}", handler.getClass().getSimpleName());
        return this;
    }

    /**
     * Unregisters a handler for incoming messages
     *
     * @param <T>        the type parameter
     * @param handlerCls the handler cls
     * @return the current instance of this class
     */
    public <T extends MessageHandler<?>>
    DefaultMessageProcessor unregisterHandler(final @NonNull Class<T> handlerCls) {
        registeredMessageReceivers.invalidate(handlerCls);
        client.getLogger().debugFine("Unregistered message receiver {}", handlerCls.getSimpleName());
        return this;
    }

    private void populateDefaultHandlers() {
        setErrorHandler(new DefaultErrorHandler<>(client, (handler, messageContext, throwable) -> {
            client.getLogger().warn("Class {} has encountered an error whilst processing a request from '{}'. [Packet Type: {}]",
                    handler.getClass(), messageContext.getSource(), throwable.getMessage()
            );
            client.getLogger().debugFine("Exception thrown: {}", ExceptionUtils.getStackTrace(throwable));
        }));
    }
}
