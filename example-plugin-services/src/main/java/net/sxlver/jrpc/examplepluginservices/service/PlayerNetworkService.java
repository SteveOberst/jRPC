package net.sxlver.jrpc.examplepluginservices.service;

import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.service.ProcedureImplementation;
import net.sxlver.jrpc.client.service.ProcedureTypes;
import net.sxlver.jrpc.client.service.ServiceDefinition;
import net.sxlver.jrpc.examplepluginservices.conversation.GetPlayerConversation;
import net.sxlver.jrpc.examplepluginservices.conversation.LocatePlayerConversation;
import net.sxlver.jrpc.examplepluginservices.conversation.model.PlayerDTO;
import net.sxlver.jrpc.examplepluginservices.conversation.oneway.SavePlayerRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerNetworkService extends ServiceDefinition {
    private final JRPCClient client;

    public PlayerNetworkService(final JRPCClient client) {
        super(client);
        this.client = client;
    }

    @ProcedureImplementation
    public GetPlayerConversation.Response getPlayer(final GetPlayerConversation.Request request) {
        final Player player = Bukkit.getPlayer(request.player);
        if(player == null) {
            return new GetPlayerConversation.Response(request, false, null);
        }

        final PlayerDTO dto = PlayerDTO.fromPlayer(player);
        return new GetPlayerConversation.Response(request, true, dto);
    }

    @ProcedureImplementation
    public @Nullable LocatePlayerConversation.Response locatePlayer(final LocatePlayerConversation.Request request) {
        final Player player = Bukkit.getPlayer(request.player);
        if(player == null) {
            return null;
        }

        return new LocatePlayerConversation.Response(request, client.getSource());
    }

    @ProcedureImplementation(type = ProcedureTypes.CONSUMER, async = true)
    public void savePlayer(final SavePlayerRequest request) {
        final PlayerDTO player = request.player;
        // save user...
        client.getLogger().info("saved user " + player);
    }
}
