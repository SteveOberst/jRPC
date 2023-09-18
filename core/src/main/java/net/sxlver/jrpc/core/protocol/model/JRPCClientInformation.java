package net.sxlver.jrpc.core.protocol.model;

public class JRPCClientInformation {

    private final String uniqueId;
    private final String type;

    public JRPCClientInformation(String uniqueId, String type) {
        this.uniqueId = uniqueId;
        this.type = type;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getType() {
        return type;
    }
}
