package net.sxlver.jrpc.core.protocol.impl;

import lombok.*;
import net.sxlver.jrpc.core.protocol.Message;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderData  {
    private int protocolVersion;
    private int messageType;
}
