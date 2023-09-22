package net.sxlver.jrpc.exampleplugin.conversation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.sxlver.jrpc.core.protocol.Packet;

import java.util.UUID;

public class LocatePlayerConversation {

    @AllArgsConstructor
    public static class Request extends Packet {
        public String player;
    }

    @AllArgsConstructor
    public static class Response extends Packet  {
        public UUID player;
        public String name;
        public String server;
    }
}
