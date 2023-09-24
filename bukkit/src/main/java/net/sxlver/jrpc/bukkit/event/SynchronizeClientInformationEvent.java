package net.sxlver.jrpc.bukkit.event;

import lombok.*;
import net.sxlver.jrpc.client.protocol.MessageContext;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
@Getter @Setter
public class SynchronizeClientInformationEvent extends Event {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private @NonNull JRPCClientInformation[] clients;
    private @NonNull MessageContext<?>context;

    public SynchronizeClientInformationEvent(final @NonNull JRPCClientInformation[] clients, final @NonNull MessageContext<?> context) {
        super(true);
        this.clients = clients;
        this.context = context;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }
}
