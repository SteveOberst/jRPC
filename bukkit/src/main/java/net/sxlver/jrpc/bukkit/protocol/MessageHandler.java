package net.sxlver.jrpc.bukkit.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import lombok.NonNull;
import net.sxlver.jrpc.bukkit.JRPCBukkitPlugin;
import net.sxlver.jrpc.client.protocol.MessageReceiver;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;

import java.util.Map;

public class MessageHandler implements MessageReceiver {

    private final Cache<Class<? extends MessageProcessor>, MessageProcessor> registeredMessageProcessors = CacheBuilder.newBuilder().build();
    private final JRPCBukkitPlugin plugin;

    public MessageHandler(final JRPCBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onReceive(final @NonNull String source,
                          final @NonNull String target,
                          final @NonNull Message.TargetType targetType,
                          final @NonNull ConversationUID conversationUID,
                          final byte[] data
    ) {
        final MessageContext context = new MessageContext(source, target, targetType, conversationUID);
        final Packet packet = PacketDataSerializer.deserializePacket(data);

        for (final Map.Entry<Class<? extends MessageProcessor>, MessageProcessor> entry : registeredMessageProcessors.asMap().entrySet()) {
            final MessageProcessor processor = entry.getValue();
            if(processor.shouldAccept(packet)) {
                processor.onReceive(context, packet);
            }
        }
    }

    public void registerMessageProcessor(final @NonNull MessageProcessor processor) {
        registeredMessageProcessors.put(processor.getClass(), processor);
    }

    public void unregisterMessageProcessor(final Class<? extends MessageProcessor> processorCls) {
        registeredMessageProcessors.invalidate(processorCls);
    }
}
