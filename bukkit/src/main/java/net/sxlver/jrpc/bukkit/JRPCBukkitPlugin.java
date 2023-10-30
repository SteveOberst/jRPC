package net.sxlver.jrpc.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class JRPCBukkitPlugin extends JavaPlugin {

    private JRPCBukkitService service;

    @Override
    public void onEnable() {
        registerService();
    }

    @Override
    public void onDisable() {
        service.shutdown();
    }

    private void registerService() {
        this.service = new JRPCBukkitService(this);
        if(!service.start()) {
            getLogger().severe("Service could not be created, exiting.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getServicesManager().register(JRPCBukkitService.class, service, this, ServicePriority.Highest);
    }
}
