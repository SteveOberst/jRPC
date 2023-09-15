package net.sxlver.jrpc.core.protocol;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum ProtocolVersion {
    V0_1(1);

    private final int versionNumber;

    ProtocolVersion(final int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    @Nullable
    public static ProtocolVersion getByVersionNumber(final int versionNumber) {
        return Arrays.stream(values())
                .filter(protocolVersion -> protocolVersion.getVersionNumber() == versionNumber)
                .findFirst()
                .orElse(null);
    }
}
