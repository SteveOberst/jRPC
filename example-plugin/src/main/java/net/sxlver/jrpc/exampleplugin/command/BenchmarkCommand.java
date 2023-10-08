package net.sxlver.jrpc.exampleplugin.command;

import lombok.SneakyThrows;
import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.core.util.StringUtil;
import net.sxlver.jrpc.exampleplugin.JRPCExamplePlugin;
import net.sxlver.jrpc.exampleplugin.conversation.BenchmarkConversation;
import net.sxlver.jrpc.exampleplugin.conversation.BroadcastMessageConversation;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BenchmarkCommand implements CommandExecutor {

    private final JRPCExamplePlugin plugin;
    private final JRPCService service;

    public BenchmarkCommand(final JRPCExamplePlugin plugin) {
        this.plugin = plugin;
        this.service = plugin.getService();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if(args.length < 2) {
            sender.sendMessage(String.format("%sNot enough arguments provided", ChatColor.RED));
            return true;
        }

        final String amount = args[0];
        final String size = args[1];
        if(!StringUtils.isNumeric(amount)) {
            sender.sendMessage(String.format("%sAmount arg and payload size must be numeric.", ChatColor.RED));
        }

        final int requests = Integer.parseInt(amount);
        final int payloadSize = Integer.parseInt(size);
        final long timestamp = System.currentTimeMillis();
        final AtomicInteger sentRequestsAmount = new AtomicInteger();
        final AtomicReference<Long> requestsSentTimestamp = new AtomicReference<>();

        sender.sendMessage(String.format("%sRunning benchmark with %s request(s).", ChatColor.GRAY, requests));
        sendRequests(requestsSentTimestamp, sentRequestsAmount,requests, payloadSize).thenRun(() -> {
            sender.sendMessage(String.format("Sent %d request(s) with a payload size of %d in %d millisecond(s).",
                    sentRequestsAmount.get(), payloadSize, requestsSentTimestamp.get() - timestamp));
        });
        return true;
    }

    private CompletableFuture<Void> sendRequests(AtomicReference<Long> requestTimestamp,
                                                 AtomicInteger sentRequests,
                                                 int requests,
                                                 int payloadSize) {
        return CompletableFuture.runAsync(() -> {
            final BenchmarkConversation.Request request = new BenchmarkConversation.Request(new byte[payloadSize]);
            for (int i = 0; i < requests; i++) {
                service.broadcast(request, BenchmarkConversation.Response.class).waitFor(20000, TimeUnit.MILLISECONDS);
                sentRequests.incrementAndGet();
            }
            requestTimestamp.set(System.currentTimeMillis());
        });
    }
}
