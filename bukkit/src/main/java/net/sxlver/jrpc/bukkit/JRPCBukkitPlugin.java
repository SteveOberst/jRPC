package net.sxlver.jrpc.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class JRPCBukkitPlugin extends JavaPlugin {

    private JRPCService service;

    @Override
    public void onEnable() {
        registerService();
    }

    @Override
    public void onDisable() {
        service.shutdown();
    }

    private void registerService() {
        this.service = new JRPCService(this);
        if(!service.start()) {
            getLogger().severe("Service could not be created, exiting.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getServicesManager().register(JRPCService.class, service, this, ServicePriority.Highest);
    }
}
