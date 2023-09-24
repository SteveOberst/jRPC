package net.sxlver.jrpc.core.protocol.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;

public class SyncRegisteredClientsConversation {
    public static class Request extends Packet {
    }

    @Getter
    @AllArgsConstructor
    public static class Response extends Packet {
        private @NonNull JRPCClientInformation[] registeredClients;
    }
}
