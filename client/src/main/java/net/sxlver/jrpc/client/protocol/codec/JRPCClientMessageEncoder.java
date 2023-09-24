package net.sxlver.jrpc.client.protocol.codec;

import net.sxlver.jrpc.core.protocol.codec.JRPCMessageEncoder;

public class JRPCClientMessageEncoder extends JRPCMessageEncoder {
    private final int versionNumber;

    public JRPCClientMessageEncoder(final int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
    protected int getVersionNumber() {
        return versionNumber;
    }
}
