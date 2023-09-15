package net.sxlver.jrpc.core.protocol;

public class JRPCHandshake {
    private final String token;
    private final String uniqueId;
    private final String type;

    public JRPCHandshake(String token, String uniqueId, String type) {
        this.token = token;
        this.uniqueId = uniqueId;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getType() {
        return type;
    }
}
