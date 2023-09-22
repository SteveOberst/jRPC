package net.sxlver.jrpc.exampleplugin.conversation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.exampleplugin.conversation.model.PlayerDTO;

import java.util.List;

public class FetchPlayerListConversation {

    @AllArgsConstructor
    public static class Request extends Packet {
        public String server;
    }

    @AllArgsConstructor
    public static class Response extends Packet  {
        public String server;
        public List<PlayerDTO> players;
    }
}
