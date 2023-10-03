package net.sxlver.jrpc.exampleclient;

import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.Conversation;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.Packet;

public class Main {
    public static void main(String[] args) {
        new Application().run();
    }
}