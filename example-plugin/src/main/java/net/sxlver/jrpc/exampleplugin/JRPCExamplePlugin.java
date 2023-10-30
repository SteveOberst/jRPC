package net.sxlver.jrpc.exampleplugin;

import net.sxlver.jrpc.bukkit.JRPCBukkitService;
import net.sxlver.jrpc.exampleplugin.command.*;
import net.sxlver.jrpc.exampleplugin.conversation.BenchmarkConversation;
import net.sxlver.jrpc.exampleplugin.conversation.BroadcastMessageConversation;
import net.sxlver.jrpc.exampleplugin.conversation.FetchPlayerListConversation;
import net.sxlver.jrpc.exampleplugin.conversation.LocatePlayerConversation;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class JRPCExamplePlugin extends JavaPlugin {

    private JRPCBukkitService service;

    @Override
    public final void onEnable() {
        this.service = getJRPCService();
        registerContent();
    }

    @Override
    public final void onDisable() {
    }

    private JRPCBukkitService getJRPCService() {
        final RegisteredServiceProvider<JRPCBukkitService> serviceProvider = Bukkit.getServicesManager().getRegistration(JRPCBukkitService.class);
        if(serviceProvider == null) {
            getLogger().severe(String.format("%s is not initialized or the plugin is not installed.", JRPCBukkitService.class));
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
        getCommand("benchmark").setExecutor(new BenchmarkCommand(this));

        // Message handler
        service.getMessageProcessor().registerHandler(new LocatePlayerConversation.RequestHandler(this));
        service.getMessageProcessor().registerHandler(new FetchPlayerListConversation.RequestHandler(this));
        service.getMessageProcessor().registerHandler(new BroadcastMessageConversation.RequestHandler(this));
        service.getMessageProcessor().registerHandler(new BenchmarkConversation.RequestHandler(this));
    }

    public JRPCBukkitService getService() {
        return service;
    }
}
