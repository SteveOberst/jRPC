package net.sxlver.jrpc.examplepluginservices;

import net.sxlver.jrpc.bukkit.JRPCBukkitService;
import net.sxlver.jrpc.examplepluginservices.command.GetPlayerCommand;
import net.sxlver.jrpc.examplepluginservices.command.LocatePlayerCommand;
import net.sxlver.jrpc.examplepluginservices.command.SavePlayerCommand;
import net.sxlver.jrpc.examplepluginservices.service.PlayerNetworkService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class JRPCServiceExamplePlugin extends JavaPlugin {

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
        service.getMessageProcessor().registerHandler(new PlayerNetworkService(service.getClient()));

        getCommand("locateplayer").setExecutor(new LocatePlayerCommand(this));
        getCommand("getplayer").setExecutor(new GetPlayerCommand(this));
        getCommand("saveplayer").setExecutor(new SavePlayerCommand(this));
    }

    public JRPCBukkitService getService() {
        return service;
    }
}
