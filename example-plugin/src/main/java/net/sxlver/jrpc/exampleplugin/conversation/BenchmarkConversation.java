package net.sxlver.jrpc.exampleplugin.conversation;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;

public class BenchmarkConversation {
    @AllArgsConstructor
    public static class Request extends Packet {
        public byte[] data;
    }

    @AllArgsConstructor
    public static class Response extends Packet {
    }

    public static class RequestHandler implements MessageHandler<Request> {

        private final JRPCService service;

        public RequestHandler(final JRPCExamplePlugin plugin) {
            this.service = plugin.getService();
        }

        @Override
        public void onReceive(@NonNull MessageContext<Request> context) {
            context.replyDirectly(new Response());
        }

        @Override
        public boolean shouldAccept(@NonNull Packet packet) {
            return packet instanceof Request;
        }
    }
}
