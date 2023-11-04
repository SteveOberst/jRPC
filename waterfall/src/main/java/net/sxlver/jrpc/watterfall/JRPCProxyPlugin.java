package net.sxlver.jrpc.watterfall;

import net.md_5.bungee.api.plugin.Plugin;
import net.sxlver.jrpc.core.config.ConfigurationManager;
import net.sxlver.jrpc.core.config.DataFolderProvider;
import net.sxlver.jrpc.server.JRPCServer;
import net.sxlver.jrpc.watterfall.config.ProxyConfiguration;
import org.apache.commons.lang3.exception.ExceptionUtils;

public final class JRPCProxyPlugin extends Plugin implements DataFolderProvider {

    private JRPCServer server;
    private ConfigurationManager configurationManager;
    private ProxyConfiguration configuration;

    @Override
    public void onEnable() {
        this.server = new JRPCServer(getDataFolder().getAbsolutePath());
        this.configurationManager = new ConfigurationManager(this);
        this.configuration = configurationManager.getConfig(ProxyConfiguration.class, true);
        server.getLogger().info("Starting internal server. Wait: {} {}", configuration.getSocketWait(), configuration.getSocketWaitTimeUnit());
        try {
            server.runAsync(configuration.getSocketWait(), configuration.getSocketWaitTimeUnit());
        }catch(final Exception exception) {
            server.getLogger().fatal("Encountered error whilst booting up server. Exception info: {}", exception.getMessage());
            server.getLogger().fatal(ExceptionUtils.getStackTrace(exception));
        }
    }

    @Override
    public void onDisable() {
        this.server.close();
    }

    public JRPCServer getServer() {
        return server;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public ProxyConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getStorage() {
        return getDataFolder().getAbsolutePath();
    }
}
