package net.sxlver.jrpc.core.protocol;

public class JRPCHandshake {
    private String token;
    private String uniqueId;
    private String type;

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
