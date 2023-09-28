package net.sxlver.jrpc.exampleplugin.command;

import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ListServerCommand implements CommandExecutor {
    private final JRPCExamplePlugin plugin;
    private final JRPCService service;

    public ListServerCommand(final JRPCExamplePlugin plugin) {
        this.plugin = plugin;
        this.service = plugin.getService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }
}
