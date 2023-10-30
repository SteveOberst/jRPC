package net.sxlver.jrpc.exampleplugin.command;

import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.core.protocol.Message;
import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import net.sxlver.jrpc.core.protocol.packet.ClusterInformationConversation;
import net.sxlver.jrpc.core.util.Callback;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class ListServerCommand implements CommandExecutor {
    private final JRPCExamplePlugin plugin;
    private final JRPCService service;

    public ListServerCommand(final JRPCExamplePlugin plugin) {
        this.plugin = plugin;
        this.service = plugin.getService();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final JRPCClient client = service.getClient();
        client.getAllServers(Callback.<ClusterInformationConversation.Response>newCallback()
                .onComplete(response -> {
                    final List<JRPCClientInformation> matches = response.matches;
                    sender.sendMessage(String.format("%sFound %d instance(s): %s", ChatColor.GREEN, matches.size(), Arrays.toString(matches.toArray())));
                })
                .onExcept(throwable -> {
                    sender.sendMessage(String.format("%s%s: %s", ChatColor.RED, throwable.getClass().getSimpleName(), throwable.getMessage()));
                })
        );
        return true;
    }
}
