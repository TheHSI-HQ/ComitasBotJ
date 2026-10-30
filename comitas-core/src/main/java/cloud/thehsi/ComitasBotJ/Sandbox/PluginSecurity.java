package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.Configuration.DataPaths;
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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Manages the {@code data/security/<uuid>.security.properties} files, the permission prompt and mount requests.
 * <p>
 * Plugins request permissions in their {@code plugin.properties}:
 * <pre>
 * permissions=network,file_storage
 * permission.network.reason=...
 * permission.network.required=true
 * permission.file_storage.reason=...
 * </pre>
 * Every requested permission, which wasn't decided on yet, is asked for once, the answer is persisted.
 * A plugin isn't loaded, if one of its required permissions isn't granted.
 */
public final class PluginSecurity {
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Sandbox");
    private static final Pattern MOUNT_NAME = Pattern.compile("[A-Za-z0-9_-]+");
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_DIRECTORY_ATTEMPTS = 3;

    private final @NotNull Object fileLock = new Object();
    private @NotNull PermissionPrompter prompter = PermissionPrompter.NON_INTERACTIVE;

    public void setPrompter(@NotNull PermissionPrompter prompter) {
        this.prompter = prompter;
    }

    @NotNull
    public static Path securityFile(@NotNull UUID uuid) {
        return DataPaths.securityFile(uuid);
    }

    /**
     * The host directory, the plugin sees as {@code /}.
     */
    @NotNull
    public static Path dataFolder(@NotNull UUID uuid) {
        return DataPaths.volume(uuid);
    }

    /**
     * A permission, requested in a plugin.properties.
     *
     * @param reason   Why the plugin needs it, shown to the server owner
     * @param required If the plugin can't work without it
     */
    public record Request(@NotNull PluginPermission permission, @Nullable String reason, boolean required) {
    }

    /**
     * Parses the permissions a plugin requests in its plugin.properties.
     *
     * @return The requested permissions, in request order
     */
    @NotNull
    public static LinkedHashMap<PluginPermission, Request> parseRequests(@NotNull Properties props, @NotNull String pluginName) {
        Set<PluginPermission> requested = new LinkedHashSet<>();
        for (String key : props.getProperty("permissions", "").split("[,\\s]+")) {
            if (key.isBlank()) continue;
            PluginPermission permission = PluginPermission.fromKey(key);
            if (permission == null) {
                logger.warn("Plugin {} requests the unknown permission '{}', it is ignored", pluginName, key);
                continue;
            }
            requested.add(permission);
        }
        // A required permission is requested, even if it is missing in the list
        for (PluginPermission permission : PluginPermission.values())
            if (parseBoolean(props.getProperty("permission." + permission.getKey() + ".required", "false")))
                requested.add(permission);

        LinkedHashMap<PluginPermission, Request> requests = new LinkedHashMap<>();
        for (PluginPermission permission : requested) {
            String reason = props.getProperty("permission." + permission.getKey() + ".reason");
            boolean required = parseBoolean(props.getProperty("permission." + permission.getKey() + ".required", "false"));
            requests.put(permission, new Request(permission, reason == null || reason.isBlank() ? null : reason.strip(), required));
        }
        return requests;
    }

    /**
     * Determines the permissions of a plugin, asking the server owner for undecided ones.
     *
     * @throws PluginPermissionException If the plugin must not be loaded, e.g. a required permission wasn't granted
     */
    @NotNull
    public PluginSandbox resolve(@NotNull Plugin.PluginMetadata metadata, @NotNull Properties pluginProperties) throws IOException {
        String name = metadata.name();
        Path file = securityFile(metadata.uuid());
        LinkedHashMap<PluginPermission, Request> requests = parseRequests(pluginProperties, name);

        Properties config = new Properties();
        boolean exists = Files.exists(file);
        if (exists) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                config.load(reader);
            }

            String configuredUuid = config.getProperty("uuid", "").strip();
            if (!configuredUuid.isEmpty() && !configuredUuid.equalsIgnoreCase(metadata.uuid().toString()))
                throw new PluginPermissionException(file + " contains the uuid " + configuredUuid + ", but belongs to "
                        + name + " (" + metadata.uuid() + "). Fix or remove the uuid line in the file.");
        }

        Map<PluginPermission, Boolean> decisions = new EnumMap<>(PluginPermission.class);
        for (PluginPermission permission : PluginPermission.values()) {
            String value = config.getProperty("permission." + permission.getKey());
            if (value != null) decisions.put(permission, parseBoolean(value));
        }

        List<Request> undecided = requests.values().stream()
                .filter(request -> !decisions.containsKey(request.permission()))
                .toList();

        Map<PluginPermission, Boolean> newDecisions = new EnumMap<>(PluginPermission.class);
        List<PluginPermission> pending = List.of();
        if (!undecided.isEmpty()) {
            Set<PluginPermission> granted = decide(metadata, undecided);
            if (granted == null) {
                // Nobody could be asked, deny for now and ask again next time
                pending = undecided.stream().map(Request::permission).toList();
            } else {
                for (Request request : undecided)
                    newDecisions.put(request.permission(), granted.contains(request.permission()));
            }
        }
        decisions.putAll(newDecisions);

        synchronized (fileLock) {
            if (!exists) {
                writeNewFile(file, metadata, decisions, pending);
            } else if (!newDecisions.isEmpty()) {
                appendDecisions(file, name, newDecisions);
            }
        }

        Set<PluginPermission> granted = EnumSet.noneOf(PluginPermission.class);
        decisions.forEach((permission, value) -> {
            if (value) granted.add(permission);
        });

        List<String> missing = requests.values().stream()
                .filter(request -> request.required() && !granted.contains(request.permission()))
                .map(request -> request.permission().getKey())
                .toList();
        if (!missing.isEmpty())
            throw new PluginPermissionException("Plugin " + name + " requires the permission(s) [" + String.join(", ", missing)
                    + "], which weren't granted. Set " + String.join(", ", missing.stream().map(key -> "permission." + key + "=true").toList())
                    + " in " + file + " and reload to load it.");

        Map<String, PluginSandbox.MountPoint> mounts = parseMounts(config, name);
        PluginSandbox sandbox = new PluginSandbox(name, metadata.uuid(), granted, mounts, dataFolder(metadata.uuid()));
        for (String key : config.stringPropertyNames())
            if (key.startsWith("denied-mount.") && parseBoolean(config.getProperty(key)))
                sandbox.addNeverMount(key.substring("denied-mount.".length()));

        logger.info("Plugin {} sandbox: granted [{}], mounts [{}]", name,
                String.join(", ", granted.stream().map(PluginPermission::getKey).toList()),
                String.join(", ", mounts.values().stream()
                        .map(m -> m.name() + (m.readOnly() ? " (read-only)" : "")).toList()));

        return sandbox;
    }

    private static boolean parseBoolean(@NotNull String value) {
        return switch (value.strip().toLowerCase(Locale.ROOT)) {
            case "true", "yes", "1" -> true;
            default -> false;
        };
    }

    /**
     * What is granted without asking, configured as {@code plugin-permission-default} in server.properties.
     */
    enum PermissionDefault {
        ASK("ask"), ALL("all"), REQUIRED("required"), NONE("none");

        private final @NotNull String key;

        PermissionDefault(@NotNull String key) {
            this.key = key;
        }

        @NotNull
        static PermissionDefault current() {
            String value = Main.conf().pluginPermissionDefault.get().strip().toLowerCase(Locale.ROOT);
            for (PermissionDefault permissionDefault : values())
                if (permissionDefault.key.equals(value)) return permissionDefault;
            logger.warn("Unknown plugin-permission-default '{}' in server.properties, expected ask, all, required or none. "
                    + "Asking instead.", value);
            return ASK;
        }
    }

    /**
     * @return The granted permissions, or null if nobody could be asked
     */
    @Nullable
    private Set<PluginPermission> decide(@NotNull Plugin.PluginMetadata metadata, @NotNull List<Request> undecided) {
        String keys = String.join(", ", undecided.stream().map(request -> request.permission().getKey()).toList());
        return switch (PermissionDefault.current()) {
            case ALL -> {
                logger.warn("Granting {} the requested permissions [{}], because plugin-permission-default is all",
                        metadata.name(), keys);
                yield grantAll(undecided);
            }
            case REQUIRED -> {
                logger.warn("Granting {} only the required permissions of [{}], because plugin-permission-default is required",
                        metadata.name(), keys);
                yield grantRequired(undecided);
            }
            case NONE -> {
                logger.warn("Denying {} the requested permissions [{}], because plugin-permission-default is none",
                        metadata.name(), keys);
                yield EnumSet.noneOf(PluginPermission.class);
            }
            case ASK -> ask(metadata, undecided);
        };
    }

    @NotNull
    private static Set<PluginPermission> grantAll(@NotNull List<Request> requests) {
        Set<PluginPermission> granted = EnumSet.noneOf(PluginPermission.class);
        requests.forEach(request -> granted.add(request.permission()));
        return granted;
    }

    @NotNull
    private static Set<PluginPermission> grantRequired(@NotNull List<Request> requests) {
        Set<PluginPermission> granted = EnumSet.noneOf(PluginPermission.class);
        requests.stream().filter(Request::required).forEach(request -> granted.add(request.permission()));
        return granted;
    }

    @NotNull
    private static String option(@NotNull String key, @NotNull String text) {
        return "[" + ConsoleColor.BOLD + key + ConsoleColor.RESET + "] " + text;
    }

    /**
     * @return The granted permissions, or null if nobody could be asked
     */
    @Nullable
    private Set<PluginPermission> ask(@NotNull Plugin.PluginMetadata metadata, @NotNull List<Request> undecided) {
        String name = metadata.name();
        boolean anyRequired = undecided.stream().anyMatch(Request::required);

        StringBuilder request = new StringBuilder()
                .append('\n').append(ConsoleColor.BOLD).append("Plugin ").append(name).append(' ').append(metadata.version())
                .append(ConsoleColor.RESET).append(" (").append(metadata.jarName()).append(") requested:\n");
        for (int i = 0; i < undecided.size(); i++) {
            Request permission = undecided.get(i);
            request.append(" - ").append(i + 1).append(". ").append(permission.permission().getDisplayName())
                    .append(permission.required() ? " (required)" : "")
                    .append(": ").append(permission.reason() == null ? "(no reason given)" : permission.reason()).append('\n');
        }

        StringJoiner numbers = new StringJoiner(",");
        for (int i = 1; i <= undecided.size(); i++) numbers.add(String.valueOf(i));
        String options = "What do you grant?\n"
                + " " + option("A", "All") + " / " + (anyRequired ? option("O", "Only Required") + " / " : "")
                + option("N", "None") + " / " + option(numbers.toString(), "Selected") + "\n"
                + " " + option("E", "All for Every Plugin") + " / " + option("R", "Only Required for Every Plugin")
                + " / " + option("X", "None for Every Plugin");

        String message = request + options;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String answer = prompter.ask(message, "> ").join();
            if (answer == null) {
                logger.warn("Plugin {} requested the permissions [{}], but there is no interactive console to ask. "
                                + "They are denied, edit {} to grant them and reload.",
                        name, String.join(", ", undecided.stream().map(r -> r.permission().getKey()).toList()),
                        securityFile(metadata.uuid()));
                return null;
            }

            Answer parsed = parseAnswer(answer, undecided);
            if (parsed != null) {
                if (parsed.everyPlugin() != null) setDefault(parsed.everyPlugin());
                return parsed.granted();
            }

            message = "Invalid answer '" + answer.strip() + "'.\n" + options;
        }

        logger.warn("No valid answer for {}, all requested permissions are denied", name);
        return EnumSet.noneOf(PluginPermission.class);
    }

    private static void setDefault(@NotNull PermissionDefault permissionDefault) {
        Main.conf().pluginPermissionDefault.set(permissionDefault.key);
        try {
            Main.conf().save();
        } catch (IOException e) {
            logger.warn("Couldn't save plugin-permission-default to server.properties", e);
        }
        logger.warn("From now on, {} requested permissions are granted to every plugin without asking. "
                        + "Set plugin-permission-default=ask in server.properties to undo this.",
                switch (permissionDefault) {
                    case ALL -> "all";
                    case REQUIRED -> "only the required";
                    default -> "no";
                });
    }

    /**
     * @param granted     The permissions granted to this plugin
     * @param everyPlugin The new default for every plugin, or null if it isn't changed
     */
    record Answer(@NotNull Set<PluginPermission> granted, @Nullable PermissionDefault everyPlugin) {
    }

    /**
     * @return The parsed answer, or null if it is invalid
     */
    @Nullable
    static Answer parseAnswer(@NotNull String answer, @NotNull List<Request> undecided) {
        String normalized = answer.strip().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "":
                return null;
            case "a", "all":
                return new Answer(grantAll(undecided), null);
            case "o", "only", "required", "only required":
                return new Answer(grantRequired(undecided), null);
            case "n", "none", "no":
                return new Answer(EnumSet.noneOf(PluginPermission.class), null);
            case "e", "every":
                return new Answer(grantAll(undecided), PermissionDefault.ALL);
            case "r":
                return new Answer(grantRequired(undecided), PermissionDefault.REQUIRED);
            case "x":
                return new Answer(EnumSet.noneOf(PluginPermission.class), PermissionDefault.NONE);
        }

        Set<PluginPermission> granted = EnumSet.noneOf(PluginPermission.class);
        for (String part : normalized.split("[,\\s]+")) {
            if (part.isEmpty()) continue;
            try {
                int index = Integer.parseInt(part);
                if (index < 1 || index > undecided.size()) return null;
                granted.add(undecided.get(index - 1).permission());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return new Answer(granted, null);
    }

    // --- Mount requests at runtime ---

    private enum MountDecision {YES, NO, NEVER}

    /**
     * Asks the server owner for a new mount point, see {@link cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager#requestMount}.
     */
    @NotNull
    public CompletableFuture<@Nullable Path> requestMount(@NotNull PluginSandbox sandbox, @NotNull String name,
                                                          @NotNull String reason, boolean readOnly) {
        if (!MOUNT_NAME.matcher(name).matches())
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Mount point names may only contain A-Z, a-z, 0-9, _ and -, not " + name));

        Path existing = sandbox.getMount(name);
        if (existing != null) return CompletableFuture.completedFuture(existing);
        if (sandbox.isNeverMount(name) || PermissionDefault.current() == PermissionDefault.NONE)
            return CompletableFuture.completedFuture(null);

        CompletableFuture<@Nullable Path> result = new CompletableFuture<>();
        CompletableFuture<@Nullable Path> running = sandbox.pendingMountRequests().putIfAbsent(name, result);
        if (running != null) return running.copy();

        askMount(sandbox, name, reason, readOnly).whenComplete((path, error) -> {
            sandbox.pendingMountRequests().remove(name, result);
            // Plugin callbacks must not run on the console thread
            if (error != null) {
                logger.warn("Mount request /mnt/{} of {} failed", name, sandbox.pluginName(), error);
                CompletableFuture.runAsync(() -> result.complete(null));
            } else {
                CompletableFuture.runAsync(() -> result.complete(path));
            }
        });
        return result.copy();
    }

    @NotNull
    private CompletableFuture<@Nullable Path> askMount(@NotNull PluginSandbox sandbox, @NotNull String name,
                                                       @NotNull String reason, boolean readOnly) {
        String message = "\n" + ConsoleColor.BOLD + "Plugin " + sandbox.pluginName() + ConsoleColor.RESET
                + " requested a directory as /mnt/" + name + (readOnly ? " (read-only)" : "") + ":\n"
                + " - " + (reason.isBlank() ? "(no reason given)" : reason.strip()) + "\n"
                + "Do you grant it?\n"
                + " " + option("Y", "Yes") + " / " + option("N", "No") + " / " + option("I", "Never");

        return askMountDecision(sandbox, name, message, message, 0).thenCompose(decision -> switch (decision) {
            case YES -> askMountDirectory(sandbox, name, readOnly,
                    "Enter the directory for /mnt/" + name + " of " + sandbox.pluginName() + " (empty to cancel):", 0);
            case NEVER -> {
                sandbox.addNeverMount(name);
                append(sandbox, "denied-mount." + name + "=true\n", "was denied the mount point /mnt/" + name);
                logger.info("Plugin {} will never be given /mnt/{}, remove denied-mount.{} from {} to undo this",
                        sandbox.pluginName(), name, name, securityFile(sandbox.uuid()));
                yield CompletableFuture.completedFuture(null);
            }
            case NO -> CompletableFuture.completedFuture(null);
        });
    }

    @NotNull
    private CompletableFuture<MountDecision> askMountDecision(@NotNull PluginSandbox sandbox, @NotNull String name,
                                                              @NotNull String question, @NotNull String message, int attempt) {
        return prompter.ask(message, "> ").thenCompose(answer -> {
            if (answer == null) {
                logger.warn("Plugin {} requested a directory as /mnt/{}, but there is no interactive console to ask. "
                        + "Add mount.{}=<directory> to {} and reload to grant it.", sandbox.pluginName(), name, name,
                        securityFile(sandbox.uuid()));
                return CompletableFuture.completedFuture(MountDecision.NO);
            }

            MountDecision decision = switch (answer.strip().toLowerCase(Locale.ROOT)) {
                case "y", "yes" -> MountDecision.YES;
                case "n", "no" -> MountDecision.NO;
                case "i", "never" -> MountDecision.NEVER;
                default -> null;
            };
            if (decision != null) return CompletableFuture.completedFuture(decision);
            if (attempt + 1 >= MAX_ATTEMPTS) return CompletableFuture.completedFuture(MountDecision.NO);
            return askMountDecision(sandbox, name, question, "Invalid answer '" + answer.strip() + "'." + question, attempt + 1);
        });
    }

    @NotNull
    private CompletableFuture<@Nullable Path> askMountDirectory(@NotNull PluginSandbox sandbox, @NotNull String name,
                                                                boolean readOnly, @NotNull String message, int attempt) {
        return prompter.ask(message, "> ").thenCompose(answer -> {
            if (answer == null || answer.isBlank()) return CompletableFuture.completedFuture(null);

            String error;
            try {
                Path hostRoot = Path.of(answer.strip()).toRealPath();
                String conflict = protectedConflict(hostRoot);
                if (!Files.isDirectory(hostRoot)) {
                    error = hostRoot + " is not a directory.";
                } else if (conflict != null) {
                    error = hostRoot + " overlaps with " + conflict + ", which must never be accessible to plugins.";
                } else {
                    sandbox.addMount(new PluginSandbox.MountPoint(name, hostRoot, readOnly));
                    append(sandbox, "mount." + name + "=" + hostRoot.toString().replace('\\', '/') + "\n"
                            + "mount." + name + ".read-only=" + readOnly + "\n", "was given the mount point /mnt/" + name);
                    logger.info("Mounted {} as /mnt/{}{} for {}", hostRoot, name, readOnly ? " (read-only)" : "",
                            sandbox.pluginName());
                    return CompletableFuture.completedFuture(sandbox.getMount(name));
                }
            } catch (Exception e) {
                error = answer.strip() + " doesn't exist.";
            }

            if (attempt + 1 >= MAX_DIRECTORY_ATTEMPTS) {
                logger.warn("{} The mount point /mnt/{} of {} is not created.", error, name, sandbox.pluginName());
                return CompletableFuture.completedFuture(null);
            }
            return askMountDirectory(sandbox, name, readOnly, error + " Enter another directory (empty to cancel):", attempt + 1);
        });
    }

    private void append(@NotNull PluginSandbox sandbox, @NotNull String lines, @NotNull String what) {
        String content = "\n# " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + ": " + sandbox.pluginName() + " " + what + "\n" + lines;
        synchronized (fileLock) {
            try {
                Files.createDirectories(DataPaths.SECURITY);
                Files.writeString(securityFile(sandbox.uuid()), content, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                logger.warn("Couldn't save the decision to {}", securityFile(sandbox.uuid()), e);
            }
        }
    }

    // --- Mount points ---

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
     * Mounts must neither contain nor be inside the data directory (bot token, plugin data, security settings and
     * plugin files), the configuration, the plugin jars, the logs or the Comitas jar.
     */
    @Nullable
    static String protectedConflict(@NotNull Path mountRoot) {
        List<Path> protectedPaths = new ArrayList<>(List.of(
                DataPaths.DATA, DataPaths.SERVER_PROPERTIES, DataPaths.PLUGINS, DataPaths.LOGS
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
                .append("# The plugin sees ").append(dataFolder(metadata.uuid()).toString().replace('\\', '/'))
                .append("/ as its root directory, it is only writable with file_storage.\n")
                .append("#\n")
                .append("# Permissions (true = granted, false = denied):\n");
        for (PluginPermission permission : PluginPermission.values())
            content.append("#   permission.").append(permission.getKey()).append(" - ")
                    .append(permission.getDisplayName()).append('\n');
        content.append("#\n")
                .append("# Mount points make host directories available to the plugin as /mnt/<name>:\n")
                .append("#   mount.<name>=<directory>        (use / or \\\\ as path separator)\n")
                .append("#   mount.<name>.read-only=true\n")
                .append("#   denied-mount.<name>=true        (the plugin may not request it anymore)\n")
                .append("# Directories overlapping data, server.properties, plugins, logs or the Comitas jar\n")
                .append("# can't be mounted.\n")
                .append("#\n")
                .append("# Changes are applied on the next reload or restart.\n")
                .append("uuid=").append(metadata.uuid()).append('\n');
        for (PluginPermission permission : PluginPermission.values()) {
            // Left commented out, so that the server owner is asked on the next interactive start
            if (pending.contains(permission)) content.append("# Requested by the plugin, not decided yet:\n#");
            content.append("permission.").append(permission.getKey()).append('=')
                    .append(decisions.getOrDefault(permission, false)).append('\n');
        }

        Files.createDirectories(DataPaths.SECURITY);
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
