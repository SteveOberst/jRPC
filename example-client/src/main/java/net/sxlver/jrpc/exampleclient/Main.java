package net.sxlver.jrpc.exampleclient;

import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;

public class Main {
    public static void main(String[] args) {
        final JRPCClient client = new JRPCClient();
        client.open();

        client.write(new HelloPacketRequest(), new MessageTarget(Message.TargetType.DIRECT, ""), HelloPacketResponse.class);
    }
}