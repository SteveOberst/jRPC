package net.sxlver.jrpc.server.protocol;

import net.sxlver.jrpc.core.protocol.ConversationUID;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;

public class ServerMessageContext<T extends Packet> {
    private final T request;
    private final JRPCServerChannelHandler source;
    private final JRPCMessage sourceMessage;

    public ServerMessageContext(final T request, final JRPCServerChannelHandler source, final JRPCMessage sourceMessage) {
        this.request = request;
        this.source = source;
        this.sourceMessage = sourceMessage;
    }

    public JRPCServerChannelHandler getSource() {
        return source;
    }

    public JRPCMessage getSourceMessage() {
        return sourceMessage;
    }

    public T getRequest() {
        return request;
    }
}
