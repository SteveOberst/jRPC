package net.sxlver.jrpc.exampleclient;

import net.sxlver.jrpc.client.JRPCClient;

public class Application {

    private final JRPCClient client;

    public Application() {
        this.client = new JRPCClient();
        client.open();
    }

    public void run() {

    }
}
