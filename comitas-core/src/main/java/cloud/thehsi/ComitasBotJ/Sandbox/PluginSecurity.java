package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Manages the {@code plugins/<Plugin Name>.security.properties} files and the permission prompt.
 * <p>
 * Plugins request permissions in their {@code plugin.properties}:
 * <pre>
 * permissions=network,file_storage
 * permission.network.reason=...
 * permission.file_storage.reason=...
 * </pre>
 * Every requested permission, which wasn't decided on yet, is asked for once, the answer is persisted.
 */
public final class PluginSecurity {
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Sandbox");
    private static final Pattern MOUNT_NAME = Pattern.compile("[A-Za-z0-9_-]+");
    private static final Path PLUGINS_DIRECTORY = Path.of("plugins");
    private static final Path PLUGIN_DATA_DIRECTORY = Path.of("plugin_data");

    private @NotNull PermissionPrompter prompter = PermissionPrompter.NON_INTERACTIVE;

    public void setPrompter(@NotNull PermissionPrompter prompter) {
        this.prompter = prompter;
    }

    @NotNull
    public static Path securityFile(@NotNull String pluginName) {
        return PLUGINS_DIRECTORY.resolve(pluginName.replaceAll("[^A-Za-z0-9._ -]", "_").strip() + ".security.properties");
    }

    @NotNull
    public static Path dataFolder(@NotNull UUID uuid) {
        return PLUGIN_DATA_DIRECTORY.resolve(uuid.toString());
    }

    /**
     * Parses the permissions a plugin requests in its plugin.properties.
     *
     * @return The requested permissions with their (optional) reason, in request order
     */
    @NotNull
    public static LinkedHashMap<PluginPermission, String> parseRequests(@NotNull Properties props, @NotNull String pluginName) {
        LinkedHashMap<PluginPermission, String> requests = new LinkedHashMap<>();
        for (String key : props.getProperty("permissions", "").split("[,\\s]+")) {
            if (key.isBlank()) continue;
            PluginPermission permission = PluginPermission.fromKey(key);
            if (permission == null) {
                logger.warn("Plugin {} requests the unknown permission '{}', it is ignored", pluginName, key);
                continue;
            }
            String reason = props.getProperty("permission." + permission.getKey() + ".reason");
            requests.put(permission, reason == null || reason.isBlank() ? null : reason.strip());
        }
        return requests;
    }

    /**
     * Determines the permissions of a plugin, asking the server owner for undecided ones.
     */
    @NotNull
    public PluginSandbox resolve(@NotNull Plugin.PluginMetadata metadata, @NotNull Properties pluginProperties) throws IOException {
        String name = metadata.name();
        Path file = securityFile(name);
        LinkedHashMap<PluginPermission, String> requests = parseRequests(pluginProperties, name);

        Properties config = new Properties();
        boolean exists = Files.exists(file);
        if (exists) {
            try (InputStream in = Files.newInputStream(file)) {
                config.load(in);
            }

            String configuredUuid = config.getProperty("uuid", "").strip();
            if (!configuredUuid.isEmpty() && !configuredUuid.equalsIgnoreCase(metadata.uuid().toString())) {
                logger.error("{} belongs to a plugin with the uuid {}, but {} has the uuid {}. "
                                + "All permissions are denied, until the file is fixed or removed.",
                        file, configuredUuid, name, metadata.uuid());
                return new PluginSandbox(name, metadata.uuid(), Set.of(), Map.of(), dataFolder(metadata.uuid()));
            }
        }

        Map<PluginPermission, Boolean> decisions = new EnumMap<>(PluginPermission.class);
        for (PluginPermission permission : PluginPermission.values()) {
            String value = config.getProperty("permission." + permission.getKey());
            if (value != null) decisions.put(permission, parseBoolean(value));
        }

        List<PluginPermission> undecided = requests.keySet().stream()
                .filter(permission -> !decisions.containsKey(permission))
                .toList();

        Map<PluginPermission, Boolean> newDecisions = new EnumMap<>(PluginPermission.class);
        List<PluginPermission> pending = List.of();
        if (!undecided.isEmpty()) {
            Set<PluginPermission> granted = ask(metadata, requests, undecided);
            if (granted == null) {
                // Nobody could be asked, deny for now and ask again next time
                pending = undecided;
            } else {
                for (PluginPermission permission : undecided)
                    newDecisions.put(permission, granted.contains(permission));
            }
        }
        decisions.putAll(newDecisions);

        if (!exists) {
            writeNewFile(file, metadata, decisions, pending);
        } else if (!newDecisions.isEmpty()) {
            appendDecisions(file, name, newDecisions);
        }

        Set<PluginPermission> granted = EnumSet.noneOf(PluginPermission.class);
        decisions.forEach((permission, value) -> {
            if (value) granted.add(permission);
        });

        Map<String, PluginSandbox.MountPoint> mounts = parseMounts(config, name);

        logger.info("Plugin {} sandbox: granted [{}], mounts [{}]", name,
                String.join(", ", granted.stream().map(PluginPermission::getKey).toList()),
                String.join(", ", mounts.values().stream()
                        .map(m -> m.name() + (m.readOnly() ? " (read-only)" : "")).toList()));

        return new PluginSandbox(name, metadata.uuid(), granted, mounts, dataFolder(metadata.uuid()));
    }

    private static boolean parseBoolean(@NotNull String value) {
        return switch (value.strip().toLowerCase(Locale.ROOT)) {
            case "true", "yes", "1" -> true;
            default -> false;
        };
    }

    /**
     * @return The granted permissions, or null if nobody could be asked
     */
    @Nullable
    private Set<PluginPermission> ask(@NotNull Plugin.PluginMetadata metadata,
                                      @NotNull Map<PluginPermission, String> requests,
                                      @NotNull List<PluginPermission> undecided) {
        String name = metadata.name();

        if (Main.conf().autoGrantPluginPermissions.get()) {
            logger.warn("Granting {} the requested permissions [{}], because plugin-auto-grant-permissions is enabled",
                    name, String.join(", ", undecided.stream().map(PluginPermission::getKey).toList()));
            return EnumSet.copyOf(undecided);
        }

        String bold = ConsoleColor.BOLD.toString();
        String reset = ConsoleColor.RESET.toString();

        StringBuilder message = new StringBuilder()
                .append('\n').append(bold).append("Plugin ").append(name).append(' ').append(metadata.version())
                .append(reset).append(" (").append(metadata.jarName()).append(") requested:\n");
        for (int i = 0; i < undecided.size(); i++) {
            PluginPermission permission = undecided.get(i);
            String reason = requests.get(permission);
            message.append(" - ").append(i + 1).append(". ").append(permission.getDisplayName())
                    .append(": ").append(reason == null ? "(no reason given)" : reason).append('\n');
        }

        StringJoiner numbers = new StringJoiner(",");
        for (int i = 1; i <= undecided.size(); i++) numbers.add(bold + i + reset);
        String prompt = "What do you grant? (" + bold + "A" + reset + "ll/" + numbers + "/" + bold + "N" + reset
                + "one/All for " + bold + "E" + reset + "very Plugin): ";

        for (int attempt = 0; attempt < 5; attempt++) {
            String answer = prompter.ask(message.toString(), prompt);
            if (answer == null) {
                logger.warn("Plugin {} requested the permissions [{}], but there is no interactive console to ask. "
                                + "They are denied, edit {} to grant them and reload.",
                        name, String.join(", ", undecided.stream().map(PluginPermission::getKey).toList()),
                        securityFile(name));
                return null;
            }

            Set<PluginPermission> granted = parseAnswer(answer, undecided);
            if (granted != null) return granted;

            if (answer.strip().equalsIgnoreCase("e") || answer.strip().equalsIgnoreCase("every")) {
                Main.conf().autoGrantPluginPermissions.set(true);
                try {
                    Main.conf().save();
                } catch (IOException e) {
                    logger.warn("Couldn't save plugin-auto-grant-permissions to server.properties", e);
                }
                logger.warn("All requested permissions will now be granted to every plugin without asking. "
                        + "Set plugin-auto-grant-permissions=false in server.properties to undo this.");
                return EnumSet.copyOf(undecided);
            }

            message.setLength(0);
            message.append("Invalid answer '").append(answer.strip()).append("'.");
        }

        logger.warn("No valid answer for {}, all requested permissions are denied", name);
        return EnumSet.noneOf(PluginPermission.class);
    }

    /**
     * @return The granted permissions, or null if the answer is invalid (or "every plugin")
     */
    @Nullable
    static Set<PluginPermission> parseAnswer(@NotNull String answer, @NotNull List<PluginPermission> undecided) {
        String normalized = answer.strip().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "a", "all":
                return EnumSet.copyOf(undecided);
            case "n", "none":
                return EnumSet.noneOf(PluginPermission.class);
            case "", "e", "every":
                return null;
        }

        Set<PluginPermission> granted = EnumSet.noneOf(PluginPermission.class);
        for (String part : normalized.split("[,\\s]+")) {
            if (part.isEmpty()) continue;
            try {
                int index = Integer.parseInt(part);
                if (index < 1 || index > undecided.size()) return null;
                granted.add(undecided.get(index - 1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return granted;
    }

    @NotNull
    private static Map<String, PluginSandbox.MountPoint> parseMounts(@NotNull Properties config, @NotNull String pluginName) {
        Map<String, PluginSandbox.MountPoint> mounts = new TreeMap<>();
        for (String key : config.stringPropertyNames()) {
            if (!key.startsWith("mount.") || key.endsWith(".read-only")) continue;
            String name = key.substring("mount.".length());
            if (!MOUNT_NAME.matcher(name).matches()) {
                logger.warn("Ignoring mount point '{}' of {}, names may only contain A-Z, a-z, 0-9, _ and -", name, pluginName);
                continue;
            }

            Path hostRoot;
            try {
                hostRoot = Path.of(config.getProperty(key).strip()).toRealPath();
            } catch (Exception e) {
                logger.warn("Ignoring mount point '{}' of {}, the directory {} doesn't exist", name, pluginName, config.getProperty(key));
                continue;
            }

            if (!Files.isDirectory(hostRoot)) {
                logger.warn("Ignoring mount point '{}' of {}, {} is not a directory", name, pluginName, hostRoot);
                continue;
            }

            String conflict = protectedConflict(hostRoot);
            if (conflict != null) {
                logger.error("Refusing mount point '{}' of {}: {} overlaps with {}, which must never be accessible to plugins",
                        name, pluginName, hostRoot, conflict);
                continue;
            }

            boolean readOnly = parseBoolean(config.getProperty(key + ".read-only", "false"));
            mounts.put(name, new PluginSandbox.MountPoint(name, hostRoot, readOnly));
        }
        return mounts;
    }

    /**
     * Mounts must neither contain nor be inside the bot token, the configuration, the plugins
     * (and their security settings), the plugin data or the Comitas jar.
     */
    @Nullable
    static String protectedConflict(@NotNull Path mountRoot) {
        List<Path> protectedPaths = new ArrayList<>(List.of(
                Path.of("tokens.secret"), Path.of("server.properties"), PLUGINS_DIRECTORY, PLUGIN_DATA_DIRECTORY,
                Path.of("logs")
        ));
        try {
            protectedPaths.add(Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (Exception e) {
            if (DebugLogging.isBasicEnabled()) DebugLogging.getLogger().debug("Couldn't locate the Comitas jar", e);
        }

        for (Path path : protectedPaths) {
            Path resolved;
            try {
                resolved = Files.exists(path) ? path.toRealPath() : path.toAbsolutePath().normalize();
            } catch (IOException e) {
                resolved = path.toAbsolutePath().normalize();
            }
            if (resolved.startsWith(mountRoot) || mountRoot.startsWith(resolved)) return resolved.toString();
        }
        return null;
    }

    private static void writeNewFile(@NotNull Path file, @NotNull Plugin.PluginMetadata metadata,
                                     @NotNull Map<PluginPermission, Boolean> decisions,
                                     @NotNull List<PluginPermission> pending) throws IOException {
        StringBuilder content = new StringBuilder()
                .append("# ComitasBotJ plugin security settings\n")
                .append("# Plugin: ").append(metadata.name()).append(' ').append(metadata.version())
                .append(" (").append(metadata.jarName()).append(")\n")
                .append("#\n")
                .append("# Permissions (true = granted, false = denied):\n");
        for (PluginPermission permission : PluginPermission.values())
            content.append("#   permission.").append(permission.getKey()).append(" - ")
                    .append(permission.getDisplayName()).append('\n');
        content.append("#\n")
                .append("# Mount points make host directories available to the plugin as /mnt/<name>:\n")
                .append("#   mount.<name>=<directory>        (use / or \\\\ as path separator)\n")
                .append("#   mount.<name>.read-only=true\n")
                .append("# Directories overlapping the bot token, server.properties, plugins, plugin_data,\n")
                .append("# logs or the Comitas jar can't be mounted.\n")
                .append("#\n")
                .append("# Changes are applied on the next reload or restart.\n")
                .append("uuid=").append(metadata.uuid()).append('\n');
        for (PluginPermission permission : PluginPermission.values()) {
            // Left commented out, so that the server owner is asked on the next interactive start
            if (pending.contains(permission)) content.append("# Requested by the plugin, not decided yet:\n#");
            content.append("permission.").append(permission.getKey()).append('=')
                    .append(decisions.getOrDefault(permission, false)).append('\n');
        }

        Files.createDirectories(PLUGINS_DIRECTORY);
        Files.writeString(file, content.toString(), StandardCharsets.UTF_8);
    }

    private static void appendDecisions(@NotNull Path file, @NotNull String pluginName,
                                        @NotNull Map<PluginPermission, Boolean> decisions) throws IOException {
        StringBuilder content = new StringBuilder("\n# ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append(": ").append(pluginName).append(" requested new permissions\n");
        decisions.forEach((permission, granted) ->
                content.append("permission.").append(permission.getKey()).append('=').append(granted).append('\n'));
        Files.writeString(file, content.toString(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }
}
