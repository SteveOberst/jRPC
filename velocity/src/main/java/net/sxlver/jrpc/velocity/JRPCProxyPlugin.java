package net.sxlver.jrpc.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.sxlver.jrpc.core.config.ConfigurationManager;
import net.sxlver.jrpc.proxycommons.ProxyConfiguration;
import net.sxlver.jrpc.proxycommons.ProxyServerWrapper;
import net.sxlver.jrpc.server.JRPCServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "jrpcproxy",
        name = "JRPCProxy",
        version = BuildConstants.VERSION,
        description = "JRPC Server running on the proxy",
        authors = {"Sxlver"}
)
public class JRPCProxyPlugin {

    @Inject
    private Logger logger;

    @Inject
    private @DataDirectory
    Path dataDirectory;

    private ProxyServerWrapper proxyServer;

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        logger.info("Starting server...");
        this.proxyServer = new ProxyServerWrapper(dataDirectory.toString());
        proxyServer.start();
        logger.info("Successfully initialized server bootstrap.");
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        logger.info("Shutting down event loop group...");
        proxyServer.stop();
        logger.info("Server stopped");
    }

    public ProxyServerWrapper getProxyServer() {
        return proxyServer;
    }
}
