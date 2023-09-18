package net.sxlver.jrpc.server;

public class Main {
    public static void main(String[] args) {
        final JRPCServer server = new JRPCServer();
        try {
            server.run();
        } catch(Exception exception) {
            System.out.println("Boot failed. A critical error occurred.");
            exception.printStackTrace();
        }finally {
            server.close();
        }
    }
}