package net.sxlver.jrpc.bukkit;

import net.sxlver.jrpc.bukkit.JRPCBukkitPlugin;
import net.sxlver.jrpc.bukkit.protocol.MessageHandler;
import net.sxlver.jrpc.client.JRPCClient;

public class JRPCService {

    private final JRPCBukkitPlugin plugin;
    private final MessageHandler messageHandler;
    private JRPCClient client;

    public JRPCService(final JRPCBukkitPlugin plugin) {
        this.plugin = plugin;
        this.messageHandler = new MessageHandler(plugin);
    }

    boolean start() {
        this.client = new JRPCClient(plugin.getDataFolder().getPath());
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

    public JRPCClient getClient() {
        return client;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
}
