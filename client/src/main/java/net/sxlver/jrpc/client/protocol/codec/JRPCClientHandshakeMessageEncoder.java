package net.sxlver.jrpc.client.protocol.codec;

import net.sxlver.jrpc.core.protocol.codec.JRPCAuthEncoder;

public class JRPCClientAuthMessageEncoder extends JRPCAuthEncoder {
    private final int versionNumber;

    public JRPCClientAuthMessageEncoder(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
    protected int getVersionNumber() {
        return versionNumber;
    }
}
