package net.sxlver.jrpc.examplepluginservices.conversation.oneway;

import lombok.AllArgsConstructor;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.examplepluginservices.conversation.model.PlayerDTO;

@AllArgsConstructor
public class SavePlayerRequest extends Packet {
    public PlayerDTO player;
}
