package net.sxlver.jrpc.core.protocol;

public class JRPCAuthentication {
    private final String token;

    public JRPCAuthentication(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
