package net.sxlver.jrpc.core.protocol;

public interface ProtocolInformationProvider {
    ProtocolVersion getProtocolVersion();

    boolean isAllowVersionMismatch();
}
