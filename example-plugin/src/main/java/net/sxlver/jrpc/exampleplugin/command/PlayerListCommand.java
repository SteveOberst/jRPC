package net.sxlver.jrpc.exampleplugin.command;

import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import net.sxlver.jrpc.exampleplugin.conversation.FetchPlayerListConversation;
import net.sxlver.jrpc.exampleplugin.conversation.model.PlayerDTO;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerListCommand implements CommandExecutor {
    private final JRPCExamplePlugin plugin;
    private final JRPCService service;

    public PlayerListCommand(final JRPCExamplePlugin plugin) {
        this.plugin = plugin;
        this.service = plugin.getService();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if(args.length < 1) {
            sender.sendMessage(String.format("%sNot enough arguments provided", ChatColor.RED));
            return true;
        }

        final String target = args[0];
        sender.sendMessage(String.format("%sFetching player list for server '%s'...", ChatColor.GRAY, target));
        service.broadcast(new FetchPlayerListConversation.Request(target), FetchPlayerListConversation.Response.class)
                .onResponse((request, context) -> {
                    final FetchPlayerListConversation.Response response = context.getResponse();
                    final List<PlayerDTO> players = response.players;
                    sender.sendMessage(String.format("%s Player online on %s: %s", ChatColor.GREEN, response.server, Arrays.toString(players.toArray())));
                })
                .onTimeout((request, messageContexts) -> {
                    sender.sendMessage(String.format("%sTarget server did not respond.", ChatColor.RED));
                })
                .waitFor(1000, TimeUnit.MILLISECONDS)
                .overrideHandlers();

        return true;
    }
}
