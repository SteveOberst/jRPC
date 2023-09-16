package net.sxlver.jrpc.core.util;

public class StringUtil {
    public static String cypherString(String input) {
        return (input == null || input.isEmpty()) ? "" : input.substring(0, input.length() / 2) + "*".repeat(input.length() / 2);
    }
}
