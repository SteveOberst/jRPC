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
@AllArgsConstructor
public class SynchronizeClientInformationEvent extends Event {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private @NonNull JRPCClientInformation[] clients;
    private @NonNull MessageContext context;

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }
}
