package net.sxlver.jrpc.exampleplugin.conversation;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class BroadcastMessageConversation {
    @AllArgsConstructor
    public static class Request extends Packet {
        public String message;
        public String permission;
    }

    @AllArgsConstructor
    public static class Response extends Packet  {
        public Request request;
    }

    public static class RequestHandler implements MessageHandler<Request> {

        private final JRPCService service;

        public RequestHandler(final JRPCExamplePlugin plugin) {
            this.service = plugin.getService();
        }

        @Override
        public void onReceive(@NonNull MessageContext<Request> context) {
            final Request request = context.getRequest();
            if(request.permission.isBlank()) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', request.message));
            }else {
                Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', request.message), request.permission);
            }
            context.replyDirectly(new Response(request));
        }

        @Override
        public boolean shouldAccept(@NonNull Packet packet) {
            return packet instanceof Request;
        }
    }
}
