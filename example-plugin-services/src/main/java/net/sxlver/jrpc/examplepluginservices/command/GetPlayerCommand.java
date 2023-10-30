package net.sxlver.jrpc.examplepluginservices.command;

import net.sxlver.jrpc.bukkit.JRPCBukkitService;
import net.sxlver.jrpc.examplepluginservices.JRPCServiceExamplePlugin;
import net.sxlver.jrpc.examplepluginservices.conversation.GetPlayerConversation;
import net.sxlver.jrpc.examplepluginservices.conversation.model.PlayerDTO;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;

public class GetPlayerCommand implements CommandExecutor {

    private final JRPCServiceExamplePlugin plugin;
    private final JRPCBukkitService service;

    public GetPlayerCommand(final JRPCServiceExamplePlugin plugin) {
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
        sender.sendMessage(String.format("%sTrying to fetch player data for '%s'...", ChatColor.GRAY, target));
        service.broadcast(new GetPlayerConversation.Request(target), GetPlayerConversation.Response.class)
                .onResponse((request, context) -> {
                    final GetPlayerConversation.Response response = context.getResponse();
                    if(!response.success) return;
                    final PlayerDTO dto = response.player;
                    sender.sendMessage(String.format("%sReceived player data: %s", ChatColor.GREEN, dto.toString()));
                })
                .onTimeout((request, messageContexts) -> {
                    sender.sendMessage(String.format("Could not find %s on the entire network.", target));
                })
                .waitFor(1000, TimeUnit.MILLISECONDS)
                .alwaysNotifyTimeout()
                .overrideHandlers();

        return true;
    }
}
