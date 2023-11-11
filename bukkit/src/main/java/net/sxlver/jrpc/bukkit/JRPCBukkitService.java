package net.sxlver.jrpc.bukkit;

import lombok.NonNull;
import net.sxlver.jrpc.client.protocol.Conversation;
import net.sxlver.jrpc.client.protocol.DefaultMessageProcessor;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.Packet;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

/**
 * The type Jrpc service.
 */
public class JRPCBukkitService {

    private final JRPCBukkitPlugin plugin;
    private DefaultMessageProcessor messageProcessor;
    private JRPCClient client;

    /**
     * Instantiates a new Jrpc service.
     *
     * @param plugin the plugin
     */
    public JRPCBukkitService(final JRPCBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Start boolean.
     *
     * @return the boolean
     */
    boolean start() {
        this.client = new JRPCClient(plugin.getDataFolder().getPath(), false);
        this.messageProcessor = new DefaultMessageProcessor(this.client);
        try {
            client.open();
            client.registerMessageReceiver(messageProcessor);
            return true;
        }catch(final Exception exception) {
            client.getLogger().fatal("Error whilst initializing " + JRPCClient.class + ". This error is non-recoverable.");
            client.getLogger().fatal(exception);
            return false;
        }
    }

    /**
     * Initiates shutdown of the client.
     */
    @Blocking
    public void shutdown() {
        if(client != null) {
            client.close();
        }
    }

    /**
     * Broadcast a message to all clients.
     *
     * @param packet the packet
     */
    public void broadcast(final @NonNull Packet packet) {
        publish(packet, new MessageTarget(Message.TargetType.ALL));
    }

    /**
     * Publish a message to the client(s) matching the provided {@link MessageTarget}
     *
     * @param packet the packet to publish
     * @param target the message target
     */
    public void publish(final @NonNull Packet packet, final MessageTarget target) {
        client.publish(packet, target);
    }

    /**
     * Broadcast a message to all clients and register a conversation awaiting the responses
     *
     * @param <TRequest>       the request type parameter
     * @param <TResponse>      the response type parameter
     * @param packet           the packet to broadcast
     * @param expectedResponse the expected response, could technically be null but this will lead to
     *                         unintended behavior.
     * @return the conversation object
     */
    public <TRequest extends Packet, TResponse extends Packet>
    Conversation<TRequest, TResponse> broadcast(final @NonNull TRequest packet,
                                                final @NotNull Class<TResponse> expectedResponse) {

         return publish(packet, expectedResponse, new MessageTarget(Message.TargetType.ALL)).enableConcurrentResponseProcessing();
    }

    /**
     * Publish conversation.
     *
     * @param <TRequest>       the request type parameter
     * @param <TResponse>      the response type parameter
     * @param packet           the packet to publish
     * @param expectedResponse the expected response, could technically be null but this will lead to
     *                         unintended behavior. if you do not expect a response, refer to
     *                         {@link #publish(Packet, MessageTarget)}.
     * @param target           the message target
     * @return the conversation
     */
    public <TRequest extends Packet, TResponse extends Packet>
    Conversation<TRequest, TResponse> publish(final @NonNull TRequest packet,
                                              final @NotNull Class<TResponse> expectedResponse,
                                              final @NonNull MessageTarget target) {

        return client.publish(packet, target, expectedResponse);
    }

    /**
     * Publish load balanced conversation.
     *
     * @param <TRequest>       the type parameter
     * @param <TResponse>      the type parameter
     * @param packet           the packet
     * @param expectedResponse the expected response
     * @param target           the target
     * @return the conversation
     */
    public <TRequest extends Packet, TResponse extends Packet>
    Conversation<TRequest, TResponse> publishLoadBalanced(final @NonNull TRequest packet,
                                                          final @NotNull Class<TResponse> expectedResponse,
                                                          final @NonNull String target) {

        return client.publish(packet, new MessageTarget(Message.TargetType.LOAD_BALANCED, target), expectedResponse);
    }


    /**
     * Gets the client's local unique id.
     *
     * @return the local unique id
     */
    public String getLocalUniqueId() {
        return client.getConfig().getUniqueId();
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    public JRPCClient getClient() {
        return client;
    }

    /**
     * Gets message processor.
     *
     * @return the message processor
     */
    public DefaultMessageProcessor getMessageProcessor() {
        return messageProcessor;
    }
}
