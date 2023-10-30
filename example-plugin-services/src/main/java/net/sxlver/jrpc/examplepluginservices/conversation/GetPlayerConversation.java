package net.sxlver.jrpc.examplepluginservices.conversation;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.examplepluginservices.conversation.model.PlayerDTO;

public class GetPlayerConversation {

    @AllArgsConstructor
    public static class Request extends Packet {
        public String player;
    }

    @AllArgsConstructor
    public static class Response extends Packet {
        public Request request;
        public boolean success;
        public PlayerDTO player;
    }
}
