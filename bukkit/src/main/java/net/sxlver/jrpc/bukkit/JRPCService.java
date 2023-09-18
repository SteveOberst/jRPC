package net.sxlver.jrpc.bukkit;

import com.google.common.collect.Lists;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;

public class JRPCService {

    private final JRPCBukkitPlugin plugin;
    private MessageHandler messageHandler;
    private JRPCClient client;

    private List<JRPCClientInformation> registeredClients = Lists.newArrayList();

    public JRPCService(final JRPCBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    boolean start() {
        this.client = new JRPCClient(plugin.getDataFolder().getPath());
        this.messageHandler = new MessageHandler(this.client);
        try {
            client.open();
            client.registerMessageReceiver(messageHandler);
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

    @ApiStatus.Internal
    public void updateRegisteredClients(final JRPCClientInformation[] clients) {
        registeredClients.clear();
        registeredClients.addAll(Arrays.asList(clients));
    }

    public JRPCClient getClient() {
        return client;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
}
