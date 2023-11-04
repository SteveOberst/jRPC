package net.sxlver.jrpc.proxycommons;

import lombok.Getter;
import lombok.Setter;
import net.sxlver.configlib.annotation.Comment;
import net.sxlver.configlib.annotation.FileLocation;
import net.sxlver.configlib.configs.yaml.YamlConfiguration;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Getter @Setter
@FileLocation(path = "config", fileName = "pluginconfig.yml")
public class ProxyConfiguration extends YamlConfiguration {
    protected ProxyConfiguration(Path path, YamlProperties properties) {
        super(path, properties);
    }

    @Comment("Time to wait for the socket to open.")
    private long socketWait = 5000;

    @Comment({
            "Unit of time to wait for socket to open.",
            "Available values:",
            "- NANOSECONDS",
            "- MICROSECONDS",
            "- MILLISECONDS",
            "- SECONDS",
            "- MINUTES",
            "- HOURS",
            "- DAYS"
    })
    private TimeUnit socketWaitTimeUnit = TimeUnit.MILLISECONDS;
}
