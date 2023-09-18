package net.sxlver.jrpc.exampleclient;

import net.sxlver.jrpc.core.protocol.Packet;

public class HelloPacketResponse implements Packet {
    public String response = "world";
}
