package net.sxlver.jrpc.exampleplugin;

import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.exampleplugin.command.BroadcastCommand;
import net.sxlver.jrpc.exampleplugin.command.ListServerCommand;
import net.sxlver.jrpc.exampleplugin.command.LocatePlayerCommand;
import net.sxlver.jrpc.exampleplugin.command.PlayerListCommand;
import net.sxlver.jrpc.exampleplugin.conversation.BroadcastMessageConversation;
import net.sxlver.jrpc.exampleplugin.conversation.FetchPlayerListConversation;
import net.sxlver.jrpc.exampleplugin.conversation.LocatePlayerConversation;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class JRPCExamplePlugin extends JavaPlugin {

    private JRPCService service;

    @Override
    public final void onEnable() {
        this.service = getJRPCService();
        registerContent();
    }

    @Override
    public final void onDisable() {
    }

    private JRPCService getJRPCService() {
        final RegisteredServiceProvider<JRPCService> serviceProvider = Bukkit.getServicesManager().getRegistration(JRPCService.class);
        if(serviceProvider == null) {
            getLogger().severe(String.format("%s is not initialized or the plugin is not installed.", JRPCService.class));
            Bukkit.getPluginManager().disablePlugin(this);
            return null;
        }
        return serviceProvider.getProvider();
    }

    private void registerContent() {
        // commands
        getCommand("locateplayer").setExecutor(new LocatePlayerCommand(this));
        getCommand("playerlist").setExecutor(new PlayerListCommand(this));
        getCommand("broadcast").setExecutor(new BroadcastCommand(this));
        getCommand("listservers").setExecutor(new ListServerCommand(this));

        // Message handler
        service.getMessageProcessor().registerHandler(new LocatePlayerConversation.LocatePlayerConversationHandler(this));
        service.getMessageProcessor().registerHandler(new FetchPlayerListConversation.FetchPlayerListConversationHandler(this));
        service.getMessageProcessor().registerHandler(new BroadcastMessageConversation.BroadcastMessageConversationHandler(this));
    }

    public JRPCService getService() {
        return service;
    }
}
