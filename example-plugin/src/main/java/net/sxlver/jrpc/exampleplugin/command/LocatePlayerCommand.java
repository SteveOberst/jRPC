package net.sxlver.jrpc.exampleplugin.command;

import net.sxlver.jrpc.bukkit.JRPCBukkitService;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import net.sxlver.jrpc.exampleplugin.conversation.LocatePlayerConversation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;

public class LocatePlayerCommand implements CommandExecutor {

    private final JRPCExamplePlugin plugin;
    private final JRPCBukkitService service;

    public LocatePlayerCommand(final JRPCExamplePlugin plugin) {
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
        sender.sendMessage(String.format("%sTrying to locate player '%s'...", ChatColor.GRAY, target));
        service.broadcast(new LocatePlayerConversation.Request(target), LocatePlayerConversation.Response.class)
                .onResponse((request, context) -> {
                    final LocatePlayerConversation.Response response = context.getResponse();
                    sender.sendMessage(String.format("%sLocated player '%s' on server '%s'. UUID: %s", ChatColor.GREEN, response.name, response.server, response.player));
                })
                .onTimeout((request, messageContexts) -> {
                    sender.sendMessage(String.format("Could not find %s on the entire network.", target));
                })
                .waitFor(1000, TimeUnit.MILLISECONDS)
                .overrideHandlers();

        return true;
    }
}
