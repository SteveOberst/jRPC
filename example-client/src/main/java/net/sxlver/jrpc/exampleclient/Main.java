package net.sxlver.jrpc.exampleclient;

import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.Conversation;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;

public class Main {
    public static void main(String[] args) {
        final JRPCClient client = new JRPCClient();
        client.open();
        final Conversation<HelloPacketRequest, HelloPacketResponse> conversation = client.write(new HelloPacketRequest(), new MessageTarget(Message.TargetType.DIRECT, ""), HelloPacketResponse.class);
        conversation.onResponse((request, response) -> {
           client.getLogger().info("received response. [Request Content: {}] [Response Content: {}]", request.request, response.response);
        });
    }
}