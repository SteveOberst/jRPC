package net.sxlver.jrpc.server.util;

import net.sxlver.jrpc.server.JRPCServer;

import java.io.File;

public class DataFolder {
    public static String getDefaultDataFolder() {
        try {
            return new File(JRPCServer.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI().getPath())
                    .getParent();
        }catch(final Exception exception) {
            throw new RuntimeException("Could not load path for default data folder.");
        }
    }
}
