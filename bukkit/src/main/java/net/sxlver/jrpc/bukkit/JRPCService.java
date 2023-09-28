package net.sxlver.jrpc.bukkit;

import lombok.NonNull;
import net.sxlver.jrpc.client.protocol.Conversation;
import net.sxlver.jrpc.client.protocol.DefaultMessageProcessor;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.Packet;

public class JRPCService {

    private final JRPCBukkitPlugin plugin;
    private DefaultMessageProcessor messageProcessor;
    private JRPCClient client;

    public JRPCService(final JRPCBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    boolean start() {
        this.client = new JRPCClient(plugin.getDataFolder().getPath(), false);
        this.messageProcessor = new DefaultMessageProcessor(this.client);
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
        publish(packet, new MessageTarget(Message.TargetType.ALL));
    }

    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> broadcast(final @NonNull TRequest packet, final Class<TResponse> expectedResponse) {
         return publish(packet, expectedResponse, new MessageTarget(Message.TargetType.ALL));
    }

    public void publish(final @NonNull Packet packet, final MessageTarget target) {
        client.write(packet, target);
    }

    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> publish(final TRequest packet, final Class<TResponse> expectedResponse, final MessageTarget target) {
        return client.write(packet, target, expectedResponse);
    }

    public <TRequest extends Packet, TResponse extends Packet> Conversation<TRequest, TResponse> publishLoadBalanced(final TRequest packet, final Class<TResponse> expectedResponse, final String target) {
        return client.write(packet, new MessageTarget(Message.TargetType.LOAD_BALANCED, target), expectedResponse);
    }


    public String getLocalUniqueId() {
        return client.getConfig().getUniqueId();
    }

    public JRPCClient getClient() {
        return client;
    }

    public DefaultMessageProcessor getMessageProcessor() {
        return messageProcessor;
    }
}
