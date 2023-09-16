package net.sxlver.jrpc.exampleclient;

import net.sxlver.jrpc.core.protocol.Packet;

public class HelloPacketRequest implements Packet {
    private String message = "Hello";
}
