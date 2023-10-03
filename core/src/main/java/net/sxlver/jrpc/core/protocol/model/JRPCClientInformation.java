package net.sxlver.jrpc.core.protocol.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Getter
@AllArgsConstructor
public class JRPCClientInformation {
    private @NonNull String uniqueId;
    private @NonNull String type;
    private String remoteAddress;
    private int remotePort;

    public InetSocketAddress getInetAddress() {
        return new InetSocketAddress(remoteAddress, remotePort);
    }

    @Override
    public String toString() {
        return "JRPCClient{" +
                "uniqueId='" + uniqueId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
