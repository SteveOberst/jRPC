package net.sxlver.jrpc.examplepluginservices.conversation;

import lombok.AllArgsConstructor;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.examplepluginservices.conversation.model.PlayerDTO;

public class LocatePlayerConversation {
    @AllArgsConstructor
    public static class Request extends Packet {
        public String player;
    }

    @AllArgsConstructor
    public static class Response extends Packet {
        public Request request;
        public String serverId;
    }
}
