package net.sxlver.jrpc.core.protocol.packet;

import lombok.Getter;
import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Packet;

@Getter
public class KeepAlivePacket extends Packet {
    private long timestamp;
    private long id;

    public KeepAlivePacket() {
        this.timestamp = System.currentTimeMillis();
        this.id = ConversationUID.next();
    }
}
