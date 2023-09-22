package net.sxlver.jrpc.exampleclient;

import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.Conversation;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.Packet;

public class Main {
    public static void main(String[] args) {
        final JRPCClient client = new JRPCClient();
        client.open();
        final Conversation<HelloPacket, Packet> conversation = client.write(new HelloPacket(), new MessageTarget(Message.TargetType.DIRECT, ""), Packet.class);
        conversation.onResponse((request, response) -> {
           client.getLogger().info("received response. [Request Content: {}] [Response type: {}]", request.request, response.getClass());
        }).onExcept((throwable, errorInformationHolder) -> {}
                ).overrideHandlers();
        client.close();

        conversation.onResponse((helloPacket, packet) -> {
                    
                })
                .onExcept((throwable, errorInformationHolder) -> {

                })
                .overrideHandlers()
                .enableConcurrentResponseProcessing();
    }
}