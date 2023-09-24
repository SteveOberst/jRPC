package net.sxlver.jrpc.server.protocol.codec;

import net.sxlver.jrpc.core.protocol.codec.JRPCMessageEncoder;

public class JRPCServerMessageEncoder extends JRPCMessageEncoder {

    private final int versionNumber;

    public JRPCServerMessageEncoder(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
    protected int getVersionNumber() {
        return versionNumber;
    }
}
