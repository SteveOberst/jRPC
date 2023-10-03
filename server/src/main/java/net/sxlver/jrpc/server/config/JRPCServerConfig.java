package net.sxlver.jrpc.server.config;


import net.sxlver.configlib.annotation.Comment;
import net.sxlver.configlib.annotation.FileLocation;
import net.sxlver.configlib.configs.yaml.YamlConfiguration;
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
    private String logLevel = Level.INFO.getName();
    public Level getLoggingLevel() { return Level.parse(logLevel); }

    private String serverId = UUID.randomUUID().toString();

    @Comment("Authentication token required for clients in order to authenticate with the server")
    private String authenticationToken = UUID.randomUUID().toString();

    @Comment("Port the server will be running on")
    private int port = 2777;

    @Comment("Whether the server will accept messages sent from a client with a different version number")
    private boolean allowVersionMismatch = false;

    @Comment("Whether a client can send a message to themselves")
    private boolean allowSelfForward = false;

    @Comment("Time span before a timeout will be triggered in seconds")
    private long readTimeout = 30;
}
