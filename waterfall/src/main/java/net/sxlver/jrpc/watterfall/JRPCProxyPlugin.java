package net.sxlver.jrpc.watterfall;

import net.md_5.bungee.api.plugin.Plugin;
import net.sxlver.jrpc.proxycommons.ProxyServerWrapper;

public final class JRPCProxyPlugin extends Plugin {

    private ProxyServerWrapper proxyServer;

    @Override
    public void onEnable() {
        this.proxyServer = new ProxyServerWrapper(getDataFolder().getAbsolutePath());
        proxyServer.start();
    }

    @Override
    public void onDisable() {
        proxyServer.stop();
    }

    public ProxyServerWrapper getProxyServer() {
        return proxyServer;
    }
}
