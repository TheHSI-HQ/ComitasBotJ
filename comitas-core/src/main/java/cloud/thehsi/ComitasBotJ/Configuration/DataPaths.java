package cloud.thehsi.ComitasBotJ.Configuration;

import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Locations of the files ComitasBotJ manages.
 * <pre>
 * data/
 * ├── token.secret                       Discord bot token
 * ├── .cache/                            Instance lock
 * ├── persistent/&lt;uuid&gt;.dat             Persistent data storage of a plugin
 * ├── security/&lt;uuid&gt;.security.properties Granted permissions &amp; mount points of a plugin
 * └── volumes/&lt;uuid&gt;/                    Files of a plugin, its root directory
 * </pre>
 */
public final class DataPaths {
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".DataPaths");

    /**
     * Everything managed by ComitasBotJ, never accessible to plugins.
     */
    public static final @NotNull Path DATA = Path.of("data");
    public static final @NotNull Path TOKEN = DATA.resolve("token.secret");
    public static final @NotNull Path CACHE = DATA.resolve(".cache");
    public static final @NotNull Path PLUGIN_DATA = DATA.resolve("persistent");
    public static final @NotNull Path SECURITY = DATA.resolve("security");
    public static final @NotNull Path VOLUMES = DATA.resolve("volumes");

    /**
     * The plugin jars.
     */
    public static final @NotNull Path PLUGINS = Path.of("plugins");
    public static final @NotNull Path LOGS = Path.of("logs");
    public static final @NotNull Path SERVER_PROPERTIES = Path.of("server.properties");

    private DataPaths() {
    }

    @NotNull
    public static Path pluginDataFile(@NotNull UUID uuid) {
        return PLUGIN_DATA.resolve(uuid + ".dat");
    }

    @NotNull
    public static Path securityFile(@NotNull UUID uuid) {
        return SECURITY.resolve(uuid + ".security.properties");
    }

    /**
     * The host directory, a plugin sees as {@code /}.
     */
    @NotNull
    public static Path volume(@NotNull UUID uuid) {
        return VOLUMES.resolve(uuid.toString());
    }

    // --- Migration from the old layout ---

    private static final Path OLD_TOKEN = Path.of("tokens.secret");
    private static final Path OLD_CACHE = Path.of(".cache");
    private static final Path OLD_PLUGIN_DATA = Path.of("plugin_data");
    private static final Path OLD_DATA_PLUGINS = DATA.resolve("plugins");
    private static final String SECURITY_SUFFIX = ".security.properties";

    /**
     * Moves files of older versions into {@link #DATA}.
     * <ul>
     *     <li>{@code tokens.secret} &rarr; {@code data/token.secret}</li>
     *     <li>{@code .cache/} &rarr; {@code data/.cache/}</li>
     *     <li>{@code plugin_data/<uuid>.dat} and {@code data/plugins/<uuid>.dat} &rarr; {@code data/persistent/<uuid>.dat}</li>
     *     <li>{@code plugin_data/<uuid>/} &rarr; {@code data/volumes/<uuid>/}</li>
     *     <li>{@code plugins/<Name>.security.properties} &rarr; {@code data/security/<uuid>.security.properties}</li>
     *     <li>{@code plugins/<Name>/} &rarr; {@code data/volumes/<uuid>/}</li>
     * </ul>
     * Nothing is overwritten, if a file exists in both places, the old one is left alone.
     */
    public static void migrate() {
        migrate(OLD_TOKEN, TOKEN);
        migrate(OLD_CACHE, CACHE);

        if (Files.isDirectory(OLD_PLUGIN_DATA)) {
            for (Path old : list(OLD_PLUGIN_DATA)) {
                String name = old.getFileName().toString();
                if (Files.isDirectory(old) && parseUuid(name) != null) {
                    migrate(old, VOLUMES.resolve(name));
                } else if (name.endsWith(".dat")) {
                    migrate(old, PLUGIN_DATA.resolve(name));
                }
            }
            deleteIfEmpty(OLD_PLUGIN_DATA);
        }

        if (Files.isDirectory(OLD_DATA_PLUGINS)) {
            for (Path old : list(OLD_DATA_PLUGINS))
                if (old.getFileName().toString().endsWith(".dat")) migrate(old, PLUGIN_DATA.resolve(old.getFileName().toString()));
            deleteIfEmpty(OLD_DATA_PLUGINS);
        }

        if (Files.isDirectory(PLUGINS)) {
            for (Path old : list(PLUGINS)) {
                String name = old.getFileName().toString();
                if (!name.endsWith(SECURITY_SUFFIX) || !Files.isRegularFile(old)) continue;

                UUID uuid = readUuid(old);
                if (uuid == null) {
                    logger.warn("Couldn't migrate {}, it doesn't contain a valid uuid. Move it to {} manually.",
                            old, SECURITY + "/<uuid>" + SECURITY_SUFFIX);
                    continue;
                }

                Path folder = PLUGINS.resolve(name.substring(0, name.length() - SECURITY_SUFFIX.length()));
                if (Files.isDirectory(folder)) migrate(folder, volume(uuid));
                migrate(old, securityFile(uuid));
            }
        }
    }

    private static void migrate(@NotNull Path old, @NotNull Path target) {
        if (!Files.exists(old, LinkOption.NOFOLLOW_LINKS)) return;
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            logger.warn("Not migrating {}, {} already exists. Remove one of them.", old, target);
            return;
        }

        try {
            Files.createDirectories(target.toAbsolutePath().getParent());
            try {
                Files.move(old, target);
            } catch (IOException e) {
                // e.g. a docker volume / bind mount, which can't be renamed
                copyTree(old, target);
                deleteTree(old);
            }
            logger.info("Moved {} to {}", old, target);
        } catch (IOException e) {
            logger.warn("Couldn't move {} to {}, move it manually: {}", old, target, e.toString());
        }
    }

    private static void copyTree(@NotNull Path source, @NotNull Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path destination = target.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(path, destination, StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS);
                }
            }
        }
    }

    private static void deleteTree(@NotNull Path path) {
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path entry : paths.sorted(Comparator.reverseOrder()).toList()) {
                try {
                    Files.deleteIfExists(entry);
                } catch (IOException ignored) {
                    // A mount point itself can't be deleted, it is left empty
                }
            }
        } catch (IOException e) {
            logger.warn("Couldn't delete {} after copying it, remove it manually", path);
        }
    }

    private static void deleteIfEmpty(@NotNull Path directory) {
        try {
            Files.deleteIfExists(directory);
        } catch (IOException ignored) {
            // Not empty or a mount point
        }
    }

    @NotNull
    private static List<Path> list(@NotNull Path directory) {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.toList();
        } catch (IOException e) {
            logger.warn("Couldn't list {} for migration", directory, e);
            return List.of();
        }
    }

    @Nullable
    private static UUID readUuid(@NotNull Path securityFile) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(securityFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            return null;
        }
        return parseUuid(properties.getProperty("uuid", "").strip());
    }

    @Nullable
    private static UUID parseUuid(@NotNull String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
