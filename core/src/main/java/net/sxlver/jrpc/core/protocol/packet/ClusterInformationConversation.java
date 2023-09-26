package net.sxlver.jrpc.core.protocol.packet;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;

import java.util.List;

public class ClusterInformationConversation {
    @AllArgsConstructor
    public static class Request extends Packet {
        public Message.TargetType type;
        /**
         * the identifier, dependent on the {@link #type}, see {@link Message.TargetType} for more information.
         *
         * @see net.sxlver.jrpc.core.protocol.Message.TargetType
         */
        public String identifier;
    }

    @AllArgsConstructor
    public static class Response extends Packet {
        public Request request;
        public List<JRPCClientInformation> matches;
    }
}
