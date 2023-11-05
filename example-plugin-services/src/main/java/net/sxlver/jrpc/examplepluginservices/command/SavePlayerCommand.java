package net.sxlver.jrpc.examplepluginservices.command;

import net.sxlver.jrpc.bukkit.JRPCBukkitService;
import net.sxlver.jrpc.examplepluginservices.JRPCServiceExamplePlugin;
import net.sxlver.jrpc.examplepluginservices.conversation.GetPlayerConversation;
import net.sxlver.jrpc.examplepluginservices.conversation.model.PlayerDTO;
import net.sxlver.jrpc.examplepluginservices.conversation.oneway.SavePlayerRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class SavePlayerCommand implements CommandExecutor {

    private final JRPCServiceExamplePlugin plugin;
    private final JRPCBukkitService service;

    public SavePlayerCommand(final JRPCServiceExamplePlugin plugin) {
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
        final Player player = Bukkit.getPlayer(target);
        if(player == null) {
            sender.sendMessage(String.format("%sPlayer '%s' is not locally online.", ChatColor.RED, target));
        }
        sender.sendMessage(String.format("%sTrying to fetch player data for '%s'...", ChatColor.GRAY, target));
        service.broadcast(new SavePlayerRequest(PlayerDTO.fromPlayer(player)));
        return true;
    }
}
