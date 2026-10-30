package cloud.thehsi.ComitasBotJ.Configuration;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings({"SameParameterValue", "unused"})
public class ServerConfig {
    private @NotNull
    static final Path CONFIG_PATH = Path.of("./server.properties");
    private @NotNull
    static final String DEFAULT_RESOURCE = "/server.properties";
    private @NotNull
    final Properties properties = new Properties();
    // Reapplied on reload, so removed keys don't break their getters
    private @NotNull
    final Properties defaults = new Properties();

    public ServerConfig() throws IOException {
        load();
    }

    @NotNull
    public ParsedServerConfig asParsed() {
        return new ParsedServerConfig(this);
    }

    private void load() throws IOException {
        // Create file from resource if missing
        if (Files.notExists(CONFIG_PATH)) {
            try (InputStream resource = getClass().getResourceAsStream(DEFAULT_RESOURCE)) {
                if (resource == null) {
                    throw new IOException(
                            "Default config resource not found: " + DEFAULT_RESOURCE);
                }

                Files.copy(resource, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Load into a fresh object, so a failed reload keeps the old values
        Properties loaded = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            loaded.load(in);
        }

        synchronized (properties) {
            properties.clear();
            properties.putAll(loaded);
            defaults.forEach(properties::putIfAbsent);
        }
    }

    private boolean asBool(@NotNull String v) {
        return switch (v.toLowerCase()) {
            case "true", "yes" -> true;
            case "false", "no" -> false;
            default -> throw new IllegalArgumentException(
                    "Invalid boolean in config: " + v);
        };
    }

    @NotNull
    private String fromBool(boolean v) {
        return Boolean.toString(v);
    }

    @NotNull
    private Double asNumber(@NotNull String v) {
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid Number in Config: " + v);
        }
    }

    @NotNull
    private String fromNumber(double v) {
        return Double.toString(v);
    }

    public int count() {
        return properties.size();
    }

    @NotNull
    public String getString(@NotNull String key) {
        return properties.getProperty(key);
    }

    /*
    GETTERS
     */

    @NotNull
    public String getString(@NotNull String key, @NotNull String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public boolean getBoolean(@NotNull String key) {
        return asBool(properties.getProperty(key));
    }

    public boolean getBoolean(@NotNull String key, boolean defaultValue) {
        return asBool(properties.getProperty(key, fromBool(defaultValue)));
    }

    public double getNumber(@NotNull String key) {
        return asNumber(properties.getProperty(key));
    }

    public double getNumber(@NotNull String key, double defaultValue) {
        return asNumber(properties.getProperty(key, fromNumber(defaultValue)));
    }

    public void set(@NotNull String key, @NotNull String value) {
        properties.setProperty(key, value);
    }

    /*
    SETTERS
     */

    public void set(@NotNull String key, boolean value) {
        properties.setProperty(key, fromBool(value));
    }

    public void set(@NotNull String key, double value) {
        properties.setProperty(key, fromNumber(value));
    }

    public void setIfNotExist(@NotNull String key, @NotNull String value) {
        defaults.setProperty(key, value);
        if (properties.getProperty(key) == null) properties.setProperty(key, value);
    }

    public void setIfNotExist(@NotNull String key, boolean value) {
        setIfNotExist(key, fromBool(value));
    }

    public void setIfNotExist(@NotNull String key, double value) {
        setIfNotExist(key, fromNumber(value));
    }

    public void save() throws IOException {
        // Written next to the config first, so a crash can't leave a half written file
        Path temp = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(temp)) {
            properties.store(out, "ComitasBotJ Config File");
        }
        try {
            Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static class ParsedServerConfig {
        public @NotNull
        final BooleanProperty enabled;
        public @NotNull
        final BooleanProperty loadPlugins;
        public @NotNull
        final StringProperty allowedPlugins;
        public @NotNull
        final StringProperty botActivityName;
        public @NotNull
        final StringProperty pluginPermissionDefault;
        public @NotNull
        final BooleanProperty pruneGuildCommands;
        public @NotNull
        final NumberProperty pluginDataQuotaMb;
        private @NotNull
        final ServerConfig cfg;

        public ParsedServerConfig(@NotNull ServerConfig cfg) {
            this.cfg = cfg;

            this.enabled = makeProperty("enabled", true);
            this.loadPlugins = makeProperty("load-plugins", true);
            this.allowedPlugins = makeProperty("allowed-plugins", "*");
            this.botActivityName = makeProperty("bot-activity-name", "ComitasBotJ");
            this.pluginPermissionDefault = makeProperty("plugin-permission-default", "ask");
            // Guild commands may belong to other applications of the same bot, so they are only removed on request
            this.pruneGuildCommands = makeProperty("prune-guild-commands", false);
            // Maximum size of each plugin data folder in MB, 0 disables the limit
            this.pluginDataQuotaMb = makeProperty("plugin-data-quota-mb", 0);
        }

        public int count() {
            return cfg.count();
        }

        public void load() throws IOException {
            cfg.load();
        }

        public void save() throws IOException {
            cfg.save();
        }

        @NotNull
        private BooleanProperty makeProperty(@NotNull String key, boolean defaultValue) {
            cfg.setIfNotExist(key, defaultValue);
            return new BooleanProperty(
                    () -> cfg.getBoolean(key),
                    v -> cfg.set(key, v)
            );
        }

        @NotNull
        private NumberProperty makeProperty(@NotNull String key, double defaultValue) {
            cfg.setIfNotExist(key, defaultValue);
            return new NumberProperty(
                    () -> cfg.getNumber(key),
                    v -> cfg.set(key, v)
            );
        }

        @NotNull
        private StringProperty makeProperty(@NotNull String key, @NotNull String defaultValue) {
            cfg.setIfNotExist(key, defaultValue);
            return new StringProperty(
                    () -> cfg.getString(key),
                    v -> cfg.set(key, v)
            );
        }
    }

    public record BooleanProperty(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean get() {
            return getter.get();
        }

        public void set(boolean value) {
            setter.accept(value);
        }
    }

    public record NumberProperty(Supplier<Double> getter, Consumer<Double> setter) {
        public double get() {
            return getter.get();
        }

        public void set(double value) {
            setter.accept(value);
        }
    }

    public record StringProperty(@NotNull Supplier<String> getter, @NotNull Consumer<String> setter) {
        @NotNull
        public String get() {
            return getter.get();
        }

        public void set(@NotNull String value) {
            setter.accept(value);
        }
    }
}
