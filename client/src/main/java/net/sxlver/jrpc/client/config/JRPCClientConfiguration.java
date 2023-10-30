package net.sxlver.jrpc.client.config;

import java.util.logging.Level;

public interface JRPCClientConfiguration {
    Level getLoggingLevel();

    String getLogLevel();

    String getUniqueId();

    String getType();

    String getAuthenticationToken();

    String getServerAddress();

    int getServerPort();

    boolean isAllowVersionMismatch();

    boolean isIgnoreHandshakeResult();

    boolean isAutoReconnect();

    int getReconnectInterval();

    boolean isQueueMessages();

    int getQueuedMessageTimeout();

    long getConversationTimeOut();

    int getMaxResponseParallelism();

    int getMaxResponseHandlingTime();
}
