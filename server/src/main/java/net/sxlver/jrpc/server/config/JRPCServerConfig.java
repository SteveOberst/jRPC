package net.sxlver.jrpc.server.config;


import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.FileLocation;
import de.exlll.configlib.configs.yaml.YamlConfiguration;
import io.netty.handler.logging.LogLevel;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;

@Getter @Setter
@FileLocation(path = "config", fileName = "config.yml")
public class JRPCServerConfig extends YamlConfiguration {

    public JRPCServerConfig(final Path path, final YamlConfiguration.YamlProperties properties) {
        super(path, properties);
    }

    @Comment("The output level of the logger")
    private String logLevel = Level.ALL.getName();//Level.INFO.getName();
    public Level getLoggingLevel() { return Level.parse(logLevel); }

    private String serverId = UUID.randomUUID().toString();

    @Comment("Authentication token required for clients in order to authenticate with the server")
    private String authenticationToken = "3e9cdaa8-0f53-4f28-85d4-1f4d03a45e6c";

    @Comment("Port the server will be running on")
    private int port = 2777;

    @Comment("Whether the server will accept messages sent from a client with a different version number")
    private boolean allowVersionMismatch = false;
}
