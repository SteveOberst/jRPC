package net.sxlver.jrpc.core.protocol.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class JRPCClientInformation {

    private @NonNull String uniqueId;
    private @NonNull String type;
}
