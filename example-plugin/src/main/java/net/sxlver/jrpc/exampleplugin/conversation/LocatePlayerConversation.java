package net.sxlver.jrpc.exampleplugin.conversation;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class LocatePlayerConversation {

    @AllArgsConstructor
    public static class Request extends Packet {
        public String player;
    }

    @AllArgsConstructor
    public static class Response extends Packet  {
        public Request request;
        public UUID player;
        public String name;
        public String server;
    }

    public static class LocatePlayerConversationHandler implements MessageHandler<Request> {

        private final JRPCService service;

        public LocatePlayerConversationHandler(final JRPCExamplePlugin plugin) {
            this.service = plugin.getService();
        }

        @Override
        public void onReceive(@NonNull MessageContext<Request> context) {
            final Request request = context.getRequest();
            final Optional<Player> playerOpt = Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(request.player)).map(Player.class::cast).findFirst();
            playerOpt.ifPresent(player -> {
                context.reply(new Response(request, player.getUniqueId(), player.getName(), service.getLocalUniqueId()));
            });
        }

        @Override
        public boolean shouldAccept(@NonNull Packet packet) {
            return packet instanceof Request;
        }
    }
}
