package cloud.thehsi.ComitasBotJ.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ServerConfig {
    public static class ParsedServerConfig {
        private final ServerConfig cfg;

        public final BooleanProperty enabled;
        public final BooleanProperty loadPlugins;

        public ParsedServerConfig(ServerConfig cfg) {
            this.cfg = cfg;

            this.enabled = makeProperty("enabled", true);
            this.loadPlugins = makeProperty("load-plugins", true);
        }

        public Integer count() {return cfg.count();}
        public void load() throws IOException {cfg.load();}
        public void save() throws IOException {cfg.save();}

        private BooleanProperty makeProperty(String key, Boolean defaultValue) {
            cfg.setIfNotExist(key, defaultValue);
            return new BooleanProperty(
                    () -> cfg.getBoolean(key),
                    v -> cfg.set(key, v)
            );
        }

        private NumberProperty makeProperty(String key, Double defaultValue) {
            cfg.setIfNotExist(key, defaultValue);
            return new NumberProperty(
                    () -> cfg.getNumber(key),
                    v -> cfg.set(key, v)
            );
        }

        private StringProperty makeProperty(String key, String defaultValue) {
            cfg.setIfNotExist(key, defaultValue);
            return new StringProperty(
                    () -> cfg.getString(key),
                    v -> cfg.set(key, v)
            );
        }
    }

    private static final Path CONFIG_PATH = Path.of("./server.properties");
    private static final String DEFAULT_RESOURCE = "/server.properties";

    private final Properties properties = new Properties();

    public ServerConfig() throws IOException {
        load();
    }

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

        // Load properties
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            properties.load(in);
        }
    }

    private boolean asBool(String v) {
        return switch (v.toLowerCase()) {
            case "true", "yes" -> true;
            case "false", "no" -> false;
            default -> throw new IllegalArgumentException(
                    "Invalid boolean in config: " + v);
        };
    }
    private String fromBool(boolean v) {return Boolean.toString(v);}

    private Double asNumber(String v) {
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid Number in Config: " + v);
        }
    }
    private String fromNumber(double v) {return Double.toString(v);}

    public Integer count() {
        return properties.size();
    }

    /*
    GETTERS
     */

    public String getString(String key) {
        return properties.getProperty(key);
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public Boolean getBoolean(String key) {
        return asBool(properties.getProperty(key));
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return asBool(properties.getProperty(key, fromBool(defaultValue)));
    }

    public Double getNumber(String key) {
        return asNumber(properties.getProperty(key));
    }

    public Double getNumber(String key, Double defaultValue) {
        return asNumber(properties.getProperty(key, fromNumber(defaultValue)));
    }

    /*
    SETTERS
     */

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public void set(String key, Boolean value) {
        properties.setProperty(key, fromBool(value));
    }

    public void set(String key, Double value) {
        properties.setProperty(key, fromNumber(value));
    }

    public void setIfNotExist(String key, String value) {
        if (!properties.contains(key)) properties.setProperty(key, value);
    }

    public void setIfNotExist(String key, Boolean value) {
        if (properties.getProperty(key) == null) properties.setProperty(key, fromBool(value));
    }

    public void setIfNotExist(String key, Double value) {
        if (!properties.contains(key)) properties.setProperty(key, fromNumber(value));
    }

    public void save() throws IOException {
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(out, "ComitasBotJ Config File");
        }
    }

    public static class BooleanProperty {
        private final Supplier<Boolean> getter;
        private final Consumer<Boolean> setter;

        BooleanProperty(Supplier<Boolean> getter,
                        Consumer<Boolean> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public boolean get() {
            return getter.get();
        }

        public void set(boolean value) {
            setter.accept(value);
        }
    }

    public static class NumberProperty {
        private final Supplier<Double> getter;
        private final Consumer<Double> setter;

        NumberProperty(Supplier<Double> getter,
                       Consumer<Double> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public double get() {
            return getter.get();
        }

        public void set(double value) {
            setter.accept(value);
        }
    }

    public static class StringProperty {
        private final Supplier<String> getter;
        private final Consumer<String> setter;

        StringProperty(Supplier<String> getter,
                       Consumer<String> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public String get() {
            return getter.get();
        }

        public void set(String value) {
            setter.accept(value);
        }
    }
}
