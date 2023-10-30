package net.sxlver.jrpc.core.protocol;

public abstract class Packet {
    /**
     * will be extracted from json string in order to deserialize the packet
     * without providing its class path
     */
    private String packetCls;
    protected Packet() {
        this.packetCls = getClass().getName();
    }
}
