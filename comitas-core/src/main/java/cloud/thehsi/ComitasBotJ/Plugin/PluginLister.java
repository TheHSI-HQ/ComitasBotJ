package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.Configuration.DataPaths;
import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Utility.ConsoleText;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static cloud.thehsi.ComitasBotJ.Plugin.PluginLoaderManager.versionId;

public class PluginLister {
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH);

    public static void listAllPlugins() {
        File pluginDir = DataPaths.PLUGINS.toFile();

        if (!pluginDir.exists()) if (!pluginDir.mkdir()) throw new RuntimeException("Couldn't create plugins folder");

        File[] jars = pluginDir.listFiles(
                f -> f.getName().endsWith(".jar")
        );

        if (jars == null)
            return;

        record PluginInfo(
                boolean compatible,
                String name,
                String version,
                String fileName,
                UUID uuid
        ) {
        }

        List<PluginInfo> plugins = new ArrayList<>();


        for (File jar : jars) {
            try {
                {
                    // Read from the jar itself, a class loader would look into the core jar first
                    Properties props = PluginLoaderManager.readPluginProperties(jar);

                    Plugin.PluginMetadata metadata =
                            Plugin.PluginMetadata.fromProperties(props, jar.getName());

                    boolean compatible = isApiTargetCompatible(
                            metadata.targetAPI(),
                            Main.getServerVersion()
                    );

                    if (!compatible)
                        logger.warn("Plugin {} only supports {}, current version is {}", metadata.name(), metadata.targetAPI(), Main.getServerVersion());

                    plugins.add(new PluginInfo(
                            compatible,
                            metadata.name(),
                            metadata.version(),
                            jar.getName(),
                            metadata.uuid()
                    ));
                }
            } catch (Exception e) {
                logger.error("{}[{}]{} {}",
                        ConsoleColor.BRIGHT_BLACK,
                        ConsoleColor.BRIGHT_BLUE
                                + jar.getName().replaceFirst("\\.jar$", "")
                                + ConsoleColor.BRIGHT_BLACK,
                        ConsoleColor.WHITE,
                        e.getLocalizedMessage()
                );
            }
        }

        String[] headers = {
                "Status",
                "Name",
                "Version",
                "File",
                "UUID"
        };

        List<String[]> rows = new ArrayList<>();

        for (PluginInfo plugin : plugins) {
            rows.add(new String[]{
                    plugin.compatible() ? "OK" : "BAD",
                    plugin.name(),
                    plugin.version(),
                    plugin.fileName(),
                    plugin.uuid().toString()
            });
        }

        int[] widths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();

            for (String[] row : rows) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        logger.info(createBorder(widths, '┌', '┬', '┐'));

        logger.info(createHeaderRow(headers, widths));

        logger.info(createBorder(widths, '├', '┼', '┤'));

        for (PluginInfo plugin : plugins) {
            String[] row = {
                    plugin.compatible() ? "OK" : "BAD",
                    plugin.name(),
                    plugin.version(),
                    plugin.fileName(),
                    plugin.uuid().toString()
            };

            logger.info(createRow(
                    row,
                    widths,
                    plugin.compatible()
            ));
        }

        logger.info(createBorder(widths, '└', '┴', '┘'));
    }

    /**
     * Lists the loaded plugins.
     *
     * @param all Also lists the jars, which aren't loaded (unloaded, disabled and errored ones), with the reason why
     */
    public static void listPlugins(boolean all) {
        PluginLoaderManager loaderManager = ((InternalPluginManager) Comitas.getPluginManager()).pluginLoaderManager();

        List<String[]> loaded = new ArrayList<>();
        for (PluginLoaderManager.LoadedInfo info : loaderManager.loadedPlugins()) {
            Plugin.PluginMetadata metadata = info.metadata();
            loaded.add(new String[]{
                    metadata.name(), metadata.version(), metadata.jarName(), metadata.uuid().toString(),
                    info.base() ? "built in" : info.enabled() ? "enabled" : "loaded"
            });
        }
        printTable("Loaded plugins", ConsoleColor.BRIGHT_GREEN,
                new String[]{"Name", "Version", "File", "UUID", "State"}, loaded);

        if (!all) return;

        Map<PluginLoaderManager.JarState, List<String[]>> notLoaded = new EnumMap<>(PluginLoaderManager.JarState.class);
        for (PluginLoaderManager.JarStatus status : loaderManager.notLoadedJars()) {
            Plugin.PluginMetadata metadata = status.metadata();
            notLoaded.computeIfAbsent(status.state(), state -> new ArrayList<>()).add(new String[]{
                    metadata == null ? "?" : metadata.name(),
                    metadata == null ? "?" : metadata.version(),
                    status.jarName(),
                    metadata == null ? "?" : metadata.uuid().toString(),
                    shorten(status.reason() == null ? "" : status.reason())
            });
        }

        String[] headers = {"Name", "Version", "File", "UUID", "Reason"};
        printTable("Unloaded plugins", ConsoleColor.BRIGHT_YELLOW, headers,
                notLoaded.getOrDefault(PluginLoaderManager.JarState.UNLOADED, List.of()));
        printTable("Disabled plugins", ConsoleColor.BRIGHT_BLACK, headers,
                notLoaded.getOrDefault(PluginLoaderManager.JarState.DISABLED, List.of()));
        printTable("Errored plugins", ConsoleColor.BRIGHT_RED, headers,
                notLoaded.getOrDefault(PluginLoaderManager.JarState.ERRORED, List.of()));
    }

    private static final int MAX_REASON_LENGTH = 120;

    @NotNull
    private static String shorten(@NotNull String reason) {
        // Reasons may contain exception messages of plugins, they must not contain escape sequences or line breaks
        String plain = ConsoleText.plain(reason).replaceAll("\\s+", " ").strip();
        return plain.length() > MAX_REASON_LENGTH ? plain.substring(0, MAX_REASON_LENGTH - 3) + "..." : plain;
    }

    private static void printTable(@NotNull String title, @NotNull ConsoleColor color, @NotNull String[] headers,
                                   @NotNull List<String[]> rows) {
        logger.info("{}{}{}{} ({}){}", ConsoleColor.BOLD, color, title, ConsoleColor.RESET + color.toString(),
                rows.size(), ConsoleColor.RESET);
        if (rows.isEmpty()) return;

        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
            for (String[] row : rows) widths[i] = Math.max(widths[i], row[i].length());
        }

        logger.info(createBorder(widths, '┌', '┬', '┐'));
        logger.info(createHeaderRow(headers, widths));
        logger.info(createBorder(widths, '├', '┼', '┤'));
        for (String[] row : rows) {
            StringBuilder line = new StringBuilder("│");
            for (int i = 0; i < row.length; i++) {
                //noinspection StringConcatenationInFormatCall
                line.append(' ').append(ConsoleColor.WHITE)
                        .append(String.format("%-" + widths[i] + "s", row[i]))
                        .append(ConsoleColor.RESET).append(" │");
            }
            logger.info(line.toString());
        }
        logger.info(createBorder(widths, '└', '┴', '┘'));
    }

    @NotNull
    private static String createBorder(int[] widths, char left, char middle, char right) {
        StringBuilder builder = new StringBuilder();

        builder.append(left);

        for (int i = 0; i < widths.length; i++) {
            builder.repeat("─", widths[i] + 2);

            if (i < widths.length - 1) {
                builder.append(middle);
            }
        }

        builder.append(right);

        return builder.toString();
    }

    @NotNull
    private static String createHeaderRow(@NotNull String[] values, int[] widths) {
        StringBuilder builder = new StringBuilder("│");

        for (int i = 0; i < values.length; i++) {
            //noinspection StringConcatenationInFormatCall
            builder.append(" ")
                    .append(String.format("%-" + widths[i] + "s", values[i]))
                    .append(" │");
        }

        return builder.toString();
    }

    @NotNull
    private static String createRow(
            @NotNull String[] values,
            int[] widths,
            boolean compatible
    ) {

        //noinspection StringConcatenationInFormatCall
        return "│ " + // Status
                ConsoleColor.BOLD +
                (compatible ? ConsoleColor.BRIGHT_GREEN : ConsoleColor.BRIGHT_RED) +
                String.format(
                        "%-" + widths[0] + "s",
                        values[0]
                ) +
                ConsoleColor.RESET +
                " │" + // Name
                " " +
                (compatible ? ConsoleColor.WHITE : ConsoleColor.BRIGHT_BLACK) +
                String.format(
                        "%-" + widths[1] + "s",
                        values[1]
                ) +
                ConsoleColor.RESET +
                " │" + // Version
                " " +
                (compatible ? ConsoleColor.WHITE : ConsoleColor.BRIGHT_BLACK) +
                String.format(
                        "%-" + widths[2] + "s",
                        values[2]
                ) +
                ConsoleColor.RESET +
                " │" + // File
                " " +
                (compatible ? ConsoleColor.WHITE : ConsoleColor.BRIGHT_BLACK) +
                String.format(
                        "%-" + widths[3] + "s",
                        values[3]
                ) +
                ConsoleColor.RESET +
                " │" + // UUID
                " " +
                (compatible ? ConsoleColor.WHITE : ConsoleColor.BRIGHT_BLACK) +
                String.format(
                        "%-" + widths[4] + "s",
                        values[4]
                ) +
                ConsoleColor.RESET +
                " │";
    }

    private static boolean isApiTargetCompatible(@NotNull String target, @NotNull String overwriteApiVersion) {
        long apiVersion = versionId(overwriteApiVersion);

        target = target.trim();
        String[] parts = target.split("-");

        if (parts.length == 1) {
            return apiVersion == versionId(parts[0]);
        }

        return versionId(parts[0]) <= apiVersion && apiVersion <= versionId(parts[1]);
    }
}
