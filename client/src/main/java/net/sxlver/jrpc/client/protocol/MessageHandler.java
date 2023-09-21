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
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationPacket;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;

import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("UnstableApiUsage")
public class MessageHandler implements DataReceiver {

    private final Cache<Class<? extends MessageReceiver>, MessageReceiver> registeredMessageReceivers = CacheBuilder.newBuilder().build();

    private final JRPCClient client;

    private ErrorHandler errorHandler;
    
    public MessageHandler(final @NonNull JRPCClient client) {
        this.client = client;
        registerMessageReceiver(new KeepAliveReceiver(client));
    }

    @Override
    @SuppressWarnings("Unchecked")
    public void onReceive(final @NonNull String source,
                          final @NonNull String target,
                          final @NonNull Message.TargetType targetType,
                          final @NonNull ConversationUID conversationUID,
                          final byte[] data
    ) {
        final JRPCClientChannelHandler netHandler = client.getNetHandler();
        final MessageContext context = new MessageContext(source, target, targetType, conversationUID);
        final Packet packet = PacketDataSerializer.deserializePacket(data);

        if(netHandler.isObserverPresent(conversationUID)) {
            final Conversation<Packet, Packet> conversation = netHandler.getAndInvalidateObserver(conversationUID);
            // Have we received an error from the other end?
            if(packet instanceof ErrorInformationHolder errorPacket) {
                // If the provided cause is null we just assign a new instance of Exception
                final Throwable t = errorPacket.getCause() != null ? errorPacket.getCause() : new Exception();
                // call the error handler
                conversation.except(t, errorPacket);
                return;
            }

            // notify handlers of the received packet
            conversation.onResponse(packet);
            // do not continue if overrideHandlers is true on the conversation
            if(conversation.shouldOverrideHandlers()) {
                return;
            }
        }

        for (final Map.Entry<Class<? extends MessageReceiver>, MessageReceiver> entry : registeredMessageReceivers.asMap().entrySet()) {
            final MessageReceiver receiver = entry.getValue();
            try {
                if(!receiver.shouldAccept(packet)) continue;
                receiver.onReceive(context, packet);
            }catch(final Exception exception) {
                errorHandler.raiseException(context, exception);
            }
        }
    }

    public void setErrorHandler(final @NonNull BiConsumer<MessageContext, Throwable> handler) {
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
