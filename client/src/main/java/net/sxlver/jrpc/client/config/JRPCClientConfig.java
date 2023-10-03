package net.sxlver.jrpc.client.config;

import net.sxlver.configlib.annotation.Comment;
import net.sxlver.configlib.annotation.FileLocation;
import net.sxlver.configlib.configs.yaml.YamlConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;

@Getter
@Setter
@FileLocation(path = "config", fileName = "config.yml")
public class JRPCClientConfig extends YamlConfiguration {

    public JRPCClientConfig(final Path path, final YamlConfiguration.YamlProperties properties) {
        super(path, properties);
    }

    @Comment("The output level of the logger")
    private String logLevel = Level.INFO.getName();
    public Level getLoggingLevel() { return Level.parse(logLevel); }

    @Comment("The Clients unique identifier. Requires no specific format, it should just be unique between all connected clients.")
    private String uniqueId = UUID.randomUUID().toString();

    @Comment({
            "This value defines the type of server in the network.",
            "It's used for load-balancing between clients and to group up multiple client instances",
            "in order to cherry-pick message targets."
    })
    private String type = "client";

    @Comment("Authentication token used to authenticate with the server")
    private String authenticationToken = UUID.randomUUID().toString();

    @Comment("IP-Address the server is running on")
    private String serverAddress = "localhost";

    @Comment("Port the server is running on")
    private int serverPort = 2777;

    @Comment("Whether the client will accept messages from a source with a different version number")
    private boolean allowVersionMismatch = false;

    @Comment("Whether to try and continue if no handshake information have been received from the server")
    private boolean ignoreHandshakeResult = false;

    @Comment("If set to true, the client will schedule a reconnect if the connection to the server has been lost")
    private boolean autoReconnect = true;

    @Comment("The interval in which the client will attempt to reconnect")
    private int reconnectInterval = 30;

    @Comment("Whether to queue messages if the socket is currently closed")
    private boolean queueMessages = true;

    @Comment("How long messages will be queued for. Set to -1 to queue until connection has been re-established.")
    private int queuedMessageTimeout = 30;

    @Comment({
            "The amount of time the client will wait for a response of the other side in ms.",
            "This is solely a default value and may be overridden within the code."
    })
    private long conversationTimeOut = 1000;
}

