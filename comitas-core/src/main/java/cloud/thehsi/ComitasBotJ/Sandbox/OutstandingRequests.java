package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Utility.ConsoleText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * The file, which replaces the permission and mount prompts, when ComitasBotJ runs with {@code --non-interactive-prompts}.
 * <p>
 * Undecided requests are written to it instead of being asked on the console. The server owner answers them in the file
 * (or sets {@code ALLOW_ALL=Y}), on the next start the answers are applied like console answers, saved to the plugins'
 * security files and removed from this file. The file is deleted, once no requests remain.
 * <p>
 * The file only lists the requests of the current run: requests, which a plugin doesn't make anymore
 * (or of plugins, which were removed), disappear when it is written again.
 * <pre>
 * [&lt;plugin uuid&gt;]
 * permission.network=?
 * discord-permission.send_messages=Y
 * mount.music=/srv/music
 * mapped-mount.shared-cache=N
 * ALLOW_ALL=N
 * </pre>
 */
public final class OutstandingRequests {
    public static final @NotNull Path DEFAULT_FILE = Path.of("outstanding-requests.txt");
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Sandbox");
    private static final String ALLOW_ALL = "ALLOW_ALL";
    private static final String RULE = "═".repeat(76);
    private static final String THIN_RULE = "─".repeat(76);
    private final @NotNull Path file;
    /**
     * The answers read from the file at startup, by plugin and setting key.
     */
    private final @NotNull Map<UUID, Map<String, String>> answers;
    /**
     * Every request, which was listed in the file at startup.
     */
    private final @NotNull Map<UUID, Map<String, String>> listed;
    private final boolean allowAll;
    private final @NotNull Map<UUID, PluginInfo> plugins = new LinkedHashMap<>();
    /**
     * The requests still waiting for an answer, by plugin and setting key.
     */
    private final @NotNull Map<UUID, LinkedHashMap<String, Entry>> outstanding = new LinkedHashMap<>();
    private boolean startupDone;
    private OutstandingRequests(@NotNull Path file, @NotNull Map<UUID, Map<String, String>> answers, boolean allowAll) {
        this.file = file;
        this.answers = answers;
        this.listed = new HashMap<>();
        answers.forEach((uuid, entries) -> listed.put(uuid, Map.copyOf(entries)));
        this.allowAll = allowAll;
    }

    /**
     * Reads the answers of an existing file, if there is one.
     */
    @NotNull
    public static OutstandingRequests load(@NotNull Path file) throws IOException {
        Map<UUID, Map<String, String>> answers = new HashMap<>();
        boolean allowAll = false;
        if (Files.exists(file)) {
            UUID section = null;
            for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("[") && line.endsWith("]")) {
                    try {
                        section = UUID.fromString(line.substring(1, line.length() - 1).strip());
                    } catch (IllegalArgumentException e) {
                        section = null;
                    }
                    continue;
                }

                int equals = line.indexOf('=');
                if (equals < 0) continue;
                String key = line.substring(0, equals).strip();
                String value = line.substring(equals + 1).strip();
                if (key.equalsIgnoreCase(ALLOW_ALL)) {
                    allowAll = isYes(value);
                } else if (section != null) {
                    answers.computeIfAbsent(section, uuid -> new HashMap<>()).put(key, value);
                }
            }
        }
        return new OutstandingRequests(file, answers, allowAll);
    }

    /**
     * Plugin controlled text on a single line: a line break would let a plugin add answers (like {@code ALLOW_ALL=Y}).
     */
    @NotNull
    static String line(@Nullable String text) {
        StringBuilder result = new StringBuilder();
        for (char c : ConsoleText.plain(text).toCharArray())
            result.append(Character.isISOControl(c) || c == ' ' || c == ' ' ? ' ' : c);
        return result.toString().strip();
    }

    static boolean isYes(@NotNull String answer) {
        return switch (answer.strip().toLowerCase(Locale.ROOT)) {
            case "y", "yes", "true", "1" -> true;
            default -> false;
        };
    }

    static boolean isNo(@NotNull String answer) {
        return switch (answer.strip().toLowerCase(Locale.ROOT)) {
            case "n", "no", "false", "0", "never" -> true;
            default -> false;
        };
    }

    @NotNull
    public Path file() {
        return file;
    }

    /**
     * Remembers the plugin's name and version for its section in the file.
     */
    synchronized void plugin(@NotNull UUID uuid, @NotNull String name, @NotNull String version, @NotNull String jarName) {
        plugins.put(uuid, new PluginInfo(name, version, jarName));
    }

    /**
     * {@code ALLOW_ALL=Y} only applies to requests, which were listed in the file, when the owner set it,
     * never to new ones (e.g. of a plugin added later on).
     *
     * @return True if the request is granted by {@code ALLOW_ALL=Y}
     */
    synchronized boolean allowAll(@NotNull UUID uuid, @NotNull String settingKey) {
        Map<String, String> plugin = listed.get(uuid);
        return allowAll && plugin != null && plugin.containsKey(settingKey);
    }

    /**
     * @return The answer to a request, null if there is none ({@code ?}, empty or not in the file)
     */
    @Nullable
    synchronized String answer(@NotNull UUID uuid, @NotNull String settingKey) {
        Map<String, String> plugin = answers.get(uuid);
        String answer = plugin == null ? null : plugin.get(settingKey);
        return answer == null || answer.isEmpty() || answer.equals("?") ? null : answer;
    }

    /**
     * The decision for a yes / no request, {@code ALLOW_ALL} grants everything, which isn't risky.
     *
     * @return True if granted, false if denied, null if it is still undecided
     */
    @Nullable
    synchronized Boolean decision(@NotNull UUID uuid, @NotNull String settingKey, boolean risky) {
        String answer = answer(uuid, settingKey);
        if (answer != null && isYes(answer)) return true;
        if (answer != null && isNo(answer)) return false;
        return !risky && allowAll(uuid, settingKey) ? Boolean.TRUE : null;
    }

    /**
     * Lists the request as outstanding, keeping an answer the owner already wrote (e.g. a directory, which didn't exist).
     */
    synchronized void add(@NotNull UUID uuid, @NotNull Entry entry) {
        String previous = answer(uuid, entry.settingKey());
        Entry stored = previous == null ? entry : new Entry(entry.kind(), entry.settingKey(), entry.title(), entry.reason(),
                entry.required(), entry.risky(), entry.warning(), previous,
                entry.problem() != null ? entry.problem() : "Invalid answer '" + previous + "'");
        outstanding.computeIfAbsent(uuid, key -> new LinkedHashMap<>()).put(entry.settingKey(), stored);
        if (startupDone) save();
    }

    /**
     * Removes an answered request.
     */
    synchronized void resolved(@NotNull UUID uuid, @NotNull String settingKey) {
        Map<String, String> plugin = answers.get(uuid);
        if (plugin != null) plugin.remove(settingKey);
        LinkedHashMap<String, Entry> entries = outstanding.get(uuid);
        if (entries != null && entries.remove(settingKey) != null && entries.isEmpty()) outstanding.remove(uuid);
        if (startupDone) save();
    }

    /**
     * @return True if the plugin has requests waiting for an answer
     */
    synchronized boolean isPending(@NotNull UUID uuid) {
        return outstanding.containsKey(uuid);
    }

    /**
     * @return How many requests wait for an answer
     */
    public synchronized int count() {
        return outstanding.values().stream().mapToInt(Map::size).sum();
    }

    /**
     * Writes the file after all plugins were loaded, from now on every change is written right away.
     *
     * @return How many requests wait for an answer
     */
    public synchronized int finishStartup() {
        startupDone = true;
        save();
        return count();
    }

    /**
     * Writes the outstanding requests, or deletes the file, if there are none.
     */
    synchronized void save() {
        try {
            if (outstanding.isEmpty()) {
                Files.deleteIfExists(file);
                return;
            }
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, render(), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Couldn't write the outstanding requests to {}", file.toAbsolutePath(), e);
        }
    }

    @NotNull
    synchronized String render() {
        StringBuilder out = new StringBuilder()
                .append("# ").append(RULE).append('\n')
                .append("#  ComitasBotJ - Outstanding Requests\n")
                .append("# ").append(RULE).append('\n')
                .append("#\n")
                .append("# ComitasBotJ runs with --non-interactive-prompts, so the requests of plugins are listed here,\n")
                .append("# instead of being asked on the console. Answer them, then start ComitasBotJ again.\n")
                .append("#\n")
                .append("#   Y  grant          N  deny          ?  decide later (the request stays in this file)\n")
                .append("#\n")
                .append("#   Mount points:        enter the host directory to mount, or N to deny it for good\n")
                .append("#   Mapped mount points: Y shares data/mapped/<name>/ with the plugin, N denies it for good\n")
                .append("#\n")
                .append("# Answers are saved to data/security/<plugin uuid>.security.properties and removed from this\n")
                .append("# file. It is deleted, once no requests remain. Don't change the [uuid] lines.\n");

        outstanding.forEach((uuid, entries) -> {
            PluginInfo info = plugins.getOrDefault(uuid, new PluginInfo("Unknown plugin", "", ""));
            out.append("\n# ").append(THIN_RULE).append('\n')
                    .append("#  Plugin: ").append(line(info.name())).append(' ').append(line(info.version()))
                    .append(info.jarName().isEmpty() ? "" : "   (" + line(info.jarName()) + ")").append('\n')
                    .append("# ").append(THIN_RULE).append('\n')
                    .append('[').append(uuid).append("]\n");

            for (Entry entry : entries.values()) {
                boolean mount = entry.kind() == Kind.MOUNT;
                out.append('\n')
                        .append("# ").append(entry.risky() && !mount ? "RISKY " : "").append(entry.kind().title).append(": ")
                        .append(line(entry.title()))
                        .append(entry.required() ? "  - REQUIRED, the plugin isn't loaded without it" : "").append('\n')
                        .append("#   Reason: ").append(entry.reason() == null ? "(none given)" : line(entry.reason())).append('\n');
                if (entry.warning() != null)
                    out.append("#   Warning: ").append(line(entry.warning())).append('\n');
                if (mount)
                    out.append("#   Answer with the host directory to mount, or N. ALLOW_ALL doesn't apply.\n");
                else if (entry.risky())
                    out.append("#   ALLOW_ALL doesn't grant this, it needs its own answer.\n");
                if (entry.problem() != null)
                    out.append("#   !! ").append(line(entry.problem())).append('\n');
                out.append(entry.settingKey()).append('=').append(line(entry.answer())).append('\n');
            }
        });

        out.append("\n# ").append(RULE).append('\n')
                .append("# !! WARNING !!\n")
                .append("# ALLOW_ALL=Y grants EVERY request above at once, without reviewing them one by one.\n")
                .append("# RISKY requests and mount points (which need a directory) are never granted by it,\n")
                .append("# answer them individually. It only applies to the requests listed when it was set, requests\n")
                .append("# added later need a new answer. It is written back as N, once it was applied.\n")
                .append("# ").append(RULE).append('\n')
                .append(ALLOW_ALL).append("=N\n");
        return out.toString();
    }

    /**
     * What a request is, shown in the file.
     */
    enum Kind {
        PERMISSION("Permission"), DISCORD_PERMISSION("Discord permission"),
        MOUNT("Mount point"), MAPPED_MOUNT("Mapped mount point");

        private final @NotNull String title;

        Kind(@NotNull String title) {
            this.title = title;
        }
    }

    /**
     * One request in the file.
     *
     * @param settingKey The key in the security file, like {@code permission.network}
     * @param title      What is requested, like {@code Network Access} or {@code /mnt/music (read-only)}
     * @param risky      Never granted by {@code ALLOW_ALL}
     * @param warning    Shown with the request, null for none
     * @param answer     The server owner's answer, {@code ?} if there is none yet
     * @param problem    Why the previous answer couldn't be applied, null if there was none
     */
    record Entry(@NotNull Kind kind, @NotNull String settingKey, @NotNull String title, @Nullable String reason,
                 boolean required, boolean risky, @Nullable String warning, @NotNull String answer,
                 @Nullable String problem) {
    }

    private record PluginInfo(@NotNull String name, @NotNull String version, @NotNull String jarName) {
    }
}
