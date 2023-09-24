package net.sxlver.jrpc.bukkit.protocol.processors;

import lombok.NonNull;
import net.sxlver.jrpc.bukkit.JRPCBukkitPlugin;
import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.bukkit.event.SynchronizeClientInformationEvent;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import net.sxlver.jrpc.core.protocol.packet.SyncRegisteredClientsConversation;
import org.bukkit.Bukkit;

import java.util.Arrays;

public class ClientInformationHandler implements MessageHandler<SyncRegisteredClientsConversation.Response> {

    private final JRPCBukkitPlugin plugin;
    private final JRPCService service;

    public ClientInformationHandler(final JRPCBukkitPlugin plugin) {
        this.plugin = plugin;
        this.service = Bukkit.getServicesManager().getRegistration(JRPCService.class).getProvider();
    }

    @Override
    public void onReceive(final @NonNull MessageContext<SyncRegisteredClientsConversation.Response> context) {
        final SyncRegisteredClientsConversation.Response registeredClientsPacket = context.getRequest();
        final JRPCClientInformation[] clients = registeredClientsPacket.getRegisteredClients();
        final SynchronizeClientInformationEvent event = new SynchronizeClientInformationEvent(clients, context);
        Bukkit.getPluginManager().callEvent(event);
        Bukkit.getLogger().info("clients: " + Arrays.toString(clients));
       // service.updateRegisteredClients(clients);
    }

    @Override
    public boolean shouldAccept(final @NonNull Packet packet) {
        return packet instanceof SyncRegisteredClientsConversation.Response;
    }
}
