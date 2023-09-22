package net.sxlver.jrpc.bukkit.protocol.processors;

import lombok.NonNull;
import net.sxlver.jrpc.bukkit.JRPCBukkitPlugin;
import net.sxlver.jrpc.bukkit.JRPCService;
import net.sxlver.jrpc.bukkit.event.SynchronizeClientInformationEvent;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.client.protocol.MessageHandler;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import net.sxlver.jrpc.core.protocol.packet.SyncRegisteredClientsPacket;
import org.bukkit.Bukkit;

public class ClientInformationReceiver implements MessageHandler<SyncRegisteredClientsPacket> {

    private final JRPCBukkitPlugin plugin;
    private final JRPCService service;

    public ClientInformationReceiver(final JRPCBukkitPlugin plugin) {
        this.plugin = plugin;
        this.service = Bukkit.getServicesManager().getRegistration(JRPCService.class).getProvider();
    }

    @Override
    public void onReceive(final @NonNull MessageContext<SyncRegisteredClientsPacket> context) {
        final SyncRegisteredClientsPacket registeredClientsPacket = context.getRequest();
        final JRPCClientInformation[] clients = registeredClientsPacket.getRegisteredClients();
        final SynchronizeClientInformationEvent event = new SynchronizeClientInformationEvent(clients, context);
        Bukkit.getPluginManager().callEvent(event);
        service.updateRegisteredClients(clients);
    }

    @Override
    public boolean shouldAccept(final @NonNull Packet packet) {
        return packet instanceof SyncRegisteredClientsPacket;
    }
}
