package net.sxlver.jrpc.client.protocol.codec;

import net.sxlver.jrpc.core.protocol.codec.JRPCHandshakeEncoder;

public class JRPCClientHandshakeEncoder extends JRPCHandshakeEncoder {
    private final int versionNumber;

    public JRPCClientHandshakeEncoder(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
    protected int getVersionNumber() {
        return versionNumber;
    }
}
