package net.sxlver.jrpc.client.config;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.FileLocation;
import de.exlll.configlib.configs.yaml.YamlConfiguration;
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

    @Comment({
            "The amount of time the client will wait for a response of the other side in ms.",
            "This is solely a default value and may be overridden within the code."
    })
    private long conversationTimeOut = 1000;
}

