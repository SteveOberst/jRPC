package net.sxlver.jrpc.client.protocol.codec;

import net.sxlver.jrpc.core.protocol.codec.JRPCHandshakeEncoder;

public class JRPCClientHandshakeMessageEncoder extends JRPCHandshakeEncoder {
    private final int versionNumber;

    public JRPCClientHandshakeMessageEncoder(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
    protected int getVersionNumber() {
        return versionNumber;
    }
}
