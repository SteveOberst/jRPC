package net.sxlver.jrpc.exampleplugin.command;

import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import net.sxlver.jrpc.exampleplugin.conversation.BroadcastMessageConversation;
import net.sxlver.jrpc.exampleplugin.conversation.LocatePlayerConversation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class BroadcastCommand implements CommandExecutor {

    private final JRPCExamplePlugin plugin;
    private final JRPCService service;

    public BroadcastCommand(final JRPCExamplePlugin plugin) {
        this.plugin = plugin;
        this.service = plugin.getService();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if(args.length < 2) {
            sender.sendMessage(String.format("%sNot enough arguments provided", ChatColor.RED));
            return true;
        }

        final String permission = args[0];
        final String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
        final BroadcastMessageConversation.Request request = new BroadcastMessageConversation.Request(message, permission);
        sender.sendMessage(String.format("%sBroadcasting message across network to players with permission %s...", ChatColor.GRAY, permission));

        service.broadcast(request, BroadcastMessageConversation.Response.class)
                .waitFor(500, TimeUnit.MILLISECONDS, true)
                .onTimeout((req, messageContexts) -> {
                    sender.sendMessage(String.format("%sBroadcasted message to %d instance(s)", ChatColor.GREEN, messageContexts.size()));
                }).alwaysNotifyTimeout();

        return true;
    }
}
