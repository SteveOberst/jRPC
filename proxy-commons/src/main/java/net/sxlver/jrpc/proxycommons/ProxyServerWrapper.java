package net.sxlver.jrpc.proxycommons;

import net.sxlver.jrpc.core.config.ConfigurationManager;
import net.sxlver.jrpc.core.config.DataFolderProvider;
import net.sxlver.jrpc.server.JRPCServer;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ProxyServerWrapper implements DataFolderProvider {

    private JRPCServer server;
    private ConfigurationManager configurationManager;
    private ProxyConfiguration configuration;

    private final String dataFolder;

    public ProxyServerWrapper(final String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void start() {
        this.server = new JRPCServer(dataFolder);
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

    public void stop() {
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
    public String getDataFolder() {
        return dataFolder;
    }
}
