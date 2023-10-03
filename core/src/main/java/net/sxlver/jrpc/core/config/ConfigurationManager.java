package net.sxlver.jrpc.core.config;

import net.sxlver.configlib.annotation.FileLocation;
import net.sxlver.configlib.configs.yaml.YamlConfiguration;
import net.sxlver.configlib.format.FieldNameFormatters;
import lombok.NonNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sxlver
 */
public class ConfigurationManager {

    private final Map<Class<? extends YamlConfiguration>, YamlConfiguration> configCache = new HashMap<>();
    private final DataFolderProvider provider;

    private final YamlConfiguration.YamlProperties defaultProperties;

    public ConfigurationManager(final DataFolderProvider provider) {
        this.provider = provider;
        this.defaultProperties = YamlConfiguration.YamlProperties.builder()
                .setPrependNewlineToComments(true)
                .setFormatter(FieldNameFormatters.LOWER_KEBAB)
                .setPrependedComments(Arrays.asList(
                        "Authors: SteveOberst",
                        "GitHub: https://github.com/SteveOberst/"
                ))
                .build();
    }

    public void loadDefaultConfigs() {

    }

    public <T extends YamlConfiguration> YamlConfiguration loadConfigurationFile(
            final @NonNull Class<T> configurationClass
    ) {
        final FileLocation fileLocation = configurationClass.getAnnotation(FileLocation.class);
        final Path path = new File(provider.getDataFolder(), fileLocation.path() + File.separator + fileLocation.fileName()).toPath();
        try {
            final Constructor<T> constructor = configurationClass.getDeclaredConstructor(Path.class, YamlConfiguration.YamlProperties.class);
            constructor.setAccessible(true);
            final YamlConfiguration configuration = constructor.newInstance(path, defaultProperties);
            saveConfigurationFile(configuration, false);
            configuration.loadAndSave();
            configCache.put(configurationClass, configuration);
            return configuration;
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public YamlConfiguration saveConfigurationFile(
            final @NonNull YamlConfiguration configuration,
            final boolean overwrite
    ) {
        final Path path = configuration.getPath();
        if(!path.toFile().exists() || overwrite) {
            configuration.save();
        }
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public <T extends YamlConfiguration> T getConfig(
            final @NonNull Class<T> configurationClass,
            final boolean instantiateOnAbsence
    ) {
        YamlConfiguration configuration = configCache.get(configurationClass);
        if(instantiateOnAbsence && configuration == null) {
            configuration = loadConfigurationFile(configurationClass);
        }
        return (T) configuration;
    }

    public <T extends YamlConfiguration> T saveConfig(
            final @NonNull Class<T> configurationClass
    ) {
        final T configuration = getConfig(configurationClass, false);
        if(configuration == null)
            throw new NullPointerException("Configuration for class " + configurationClass + " does not exist in the cache.");

        configuration.save();
        return configuration;
    }

    public void saveAll() {
        configCache.keySet().forEach(this::saveConfig);
    }
}
