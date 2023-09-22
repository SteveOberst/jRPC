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
import java.util.function.BiConsumer;

@SuppressWarnings("UnstableApiUsage")
public class MessageHandler implements RawDataReceiver {

    private final Cache<Class<? extends MessageReceiver>, MessageReceiver> registeredMessageReceivers = CacheBuilder.newBuilder().build();

    private final JRPCClient client;

    private ErrorHandler errorHandler;
    
    public MessageHandler(final @NonNull JRPCClient client) {
        this.client = client;
        registerMessageReceiver(new KeepAliveReceiver(client));
        setErrorHandler(new DefaultErrorHandler(client, (messageReceiver, messageContext, throwable) -> {
            client.getLogger().warn("{} encountered an error whilst processing message. [Source: {}] {}", messageContext.getSource(), throwable.getMessage());
            client.getLogger().fatal(ExceptionUtils.getStackTrace(throwable));
        }));
    }

    @Override
    @SuppressWarnings("Unchecked")
    public void onReceive(final @NonNull String source,
                          final @NonNull String target,
                          final @NonNull Message.TargetType targetType,
                          final @NonNull ConversationUID conversationUID,
                          final byte[] data) {
        final JRPCClientChannelHandler netHandler = client.getNetHandler();
        final MessageContext context = new MessageContext(source, target, targetType, conversationUID);
        final Packet packet = PacketDataSerializer.deserializePacket(data);

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

        for (final Map.Entry<Class<? extends MessageReceiver>, MessageReceiver> entry : registeredMessageReceivers.asMap().entrySet()) {
            final MessageReceiver receiver = entry.getValue();
            try {
                if(!receiver.shouldAccept(packet)) continue;
                receiver.onReceive(context, packet);
            }catch(final Exception exception) {
                errorHandler.raiseException(receiver, context, exception);
            }
        }
    }

    public void setErrorHandler(final @NonNull TriConsumer<MessageReceiver, MessageContext, Throwable> handler) {
        setErrorHandler(new DefaultErrorHandler(client, handler));
    }

    public void setErrorHandler(final @NonNull ErrorHandler errorHandler) {
        registerMessageReceiver(errorHandler);
        this.errorHandler = errorHandler;
    }

    private void invalidateErrorHandler() {
        unregisterMessageReceiver(this.errorHandler.getClass());
        this.errorHandler = null;
    }

    public void registerMessageReceiver(final @NonNull MessageReceiver receiver) {
        registeredMessageReceivers.put(receiver.getClass(), receiver);
        client.getLogger().debug("Registered message receiver {}", receiver.getClass());
    }

    public void unregisterMessageReceiver(final @NonNull Class<? extends MessageReceiver> receiverCls) {
        registeredMessageReceivers.invalidate(receiverCls);
        client.getLogger().debug("Unregistered message receiver {}", receiverCls);
    }
}
