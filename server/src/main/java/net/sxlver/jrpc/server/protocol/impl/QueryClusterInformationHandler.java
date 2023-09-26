package net.sxlver.jrpc.server.protocol.impl;

import net.sxlver.jrpc.core.protocol.MessageTarget;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import net.sxlver.jrpc.core.protocol.packet.ClusterInformationConversation;
import net.sxlver.jrpc.server.JRPCServer;
import net.sxlver.jrpc.server.model.JRPCClientInstance;
import net.sxlver.jrpc.server.protocol.JRPCServerChannelHandler;
import net.sxlver.jrpc.server.protocol.ServerMessageHandler;
import net.sxlver.jrpc.server.selector.TargetSelectors;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class QueryClusterInformationHandler extends ServerMessageHandler<ClusterInformationConversation.Request> {
    public QueryClusterInformationHandler() {
        super(ClusterInformationConversation.Request.class, (server, context) -> {
            final JRPCServerChannelHandler netHandler = context.getSource();
            final ClusterInformationConversation.Request request = context.getRequest();
            final List<JRPCClientInformation> cherryPicked = TargetSelectors.getByTargetType(request.type)
                    .select(request.identifier, server.getRegisteredClientsRaw())
                    .stream()
                    .map(JRPCClientInstance::getInformation)
                    .toList();

            final ClusterInformationConversation.Response response = new ClusterInformationConversation.Response(request, cherryPicked);
            netHandler.write(response);
        });
    }
}
