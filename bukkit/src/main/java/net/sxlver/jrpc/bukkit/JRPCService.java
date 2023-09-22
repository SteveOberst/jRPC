package net.sxlver.jrpc.bukkit;

import com.google.common.collect.Lists;
import lombok.NonNull;
import net.sxlver.jrpc.client.protocol.Conversation;
import net.sxlver.jrpc.client.protocol.MessageProcessor;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;

public class JRPCService {

    private final JRPCBukkitPlugin plugin;
    private MessageProcessor messageProcessor;
    private JRPCClient client;

    private List<JRPCClientInformation> registeredClients = Lists.newArrayList();

    public JRPCService(final JRPCBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    boolean start() {
        this.client = new JRPCClient(plugin.getDataFolder().getPath());
        this.messageProcessor = new MessageProcessor(this.client);
        try {
            client.open();
            client.registerMessageReceiver(messageProcessor);
            return true;
        }catch(final Exception exception) {
            plugin.getLogger().severe("Error whilst initializing " + JRPCClient.class + ". This error is non-recoverable.");
            return false;
        }
    }

    public void shutdown() {
        if(client != null) {
            client.close();
        }
    }

    public void broadcast(final @NonNull Packet packet) {
        publish(packet, new MessageTarget(Message.TargetType.BROADCAST));
    }

    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> broadcast(final @NonNull TRequest packet, final Class<TResponse> expectedResponse) {
         return publish(packet, new MessageTarget(Message.TargetType.BROADCAST), expectedResponse);
    }

    public void publish(final @NonNull Packet packet, final MessageTarget target) {
        client.write(packet, target);
    }

    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> publish(final TRequest packet, final MessageTarget target, final Class<TResponse> expectedResponse) {
        return client.write(packet, target, expectedResponse);
    }

    @ApiStatus.Internal
    public void updateRegisteredClients(final JRPCClientInformation[] clients) {
        registeredClients.clear();
        registeredClients.addAll(Arrays.asList(clients));
    }

    public JRPCClient getClient() {
        return client;
    }

    public MessageProcessor getMessageProcessor() {
        return messageProcessor;
    }
}
