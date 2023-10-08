package net.sxlver.jrpc.exampleplugin.conversation;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import net.sxlver.jrpc.exampleplugin.conversation.model.PlayerDTO;
import org.bukkit.Bukkit;

import java.util.List;

public class FetchPlayerListConversation {

    @AllArgsConstructor
    public static class Request extends Packet {
        public String server;
    }

    @AllArgsConstructor
    public static class Response extends Packet  {
        public Request request;
        public List<PlayerDTO> players;
    }

    public static class RequestHandler implements MessageHandler<Request> {

        private final JRPCService service;

        public RequestHandler(final JRPCExamplePlugin plugin) {
            this.service = plugin.getService();
        }

        @Override
        public void onReceive(@NonNull MessageContext<Request> context) {
            final Request request = context.getRequest();
            final List<PlayerDTO> players = Bukkit.getOnlinePlayers().stream().map(PlayerDTO::fromPlayer).toList();
            context.replyDirectly(new Response(request, players));
        }

        @Override
        public boolean shouldAccept(@NonNull Packet packet) {
            return packet instanceof Request;
        }
    }
}
