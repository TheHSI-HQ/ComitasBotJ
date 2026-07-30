package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static cloud.thehsi.ComitasBotJ.Plugin.PluginLoaderManager.versionId;

public class PluginLister {
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH);

    public static void listAllPlugins() {
        File pluginDir = new File("plugins");

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
                try (URLClassLoader loader = new URLClassLoader(
                        new URL[]{jar.toURI().toURL()},
                        PluginLister.class.getClassLoader()
                )) {
                    InputStream is = loader.getResourceAsStream("plugin.properties");

                    if (is == null) {
                        throw new IllegalStateException("Missing plugin.properties");
                    }

                    Properties props = new Properties();
                    props.load(is);

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

    public static void listPlugins() {
        record PluginInfo(
                String name,
                String version,
                String fileName,
                UUID uuid
        ) {
        }

        List<PluginInfo> plugins = new ArrayList<>();


        for (Plugin.PluginMetadata metadata : Comitas.getPluginManager().getAllPluginMetadata()) {
            plugins.add(new PluginInfo(
                    metadata.name(),
                    metadata.version(),
                    metadata.jarName(),
                    metadata.uuid()
            ));
        }

        String[] headers = {
                "Name",
                "Version",
                "File",
                "UUID"
        };

        List<String[]> rows = new ArrayList<>();

        for (PluginInfo plugin : plugins) {
            rows.add(new String[]{
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

        logger.info("Plugins:");

        logger.info(createBorder(widths, '┌', '┬', '┐'));

        logger.info(createHeaderRow(headers, widths));

        logger.info(createBorder(widths, '├', '┼', '┤'));

        for (PluginInfo plugin : plugins) {
            String[] row = {
                    plugin.name(),
                    plugin.version(),
                    plugin.fileName(),
                    plugin.uuid().toString()
            };

            logger.info(createRow(
                    row,
                    widths
            ));
        }

        logger.info(createBorder(widths, '└', '┴', '┘'));
    }

    private static String createBorder(int[] widths, char left, char middle, char right) {
        StringBuilder builder = new StringBuilder();

        builder.append(left);

        for (int i = 0; i < widths.length; i++) {
            builder.append("─".repeat(widths[i] + 2));

            if (i < widths.length - 1) {
                builder.append(middle);
            }
        }

        builder.append(right);

        return builder.toString();
    }

    private static String createHeaderRow(String[] values, int[] widths) {
        StringBuilder builder = new StringBuilder("│");

        for (int i = 0; i < values.length; i++) {
            builder.append(" ")
                    .append(String.format("%-" + widths[i] + "s", values[i]))
                    .append(" │");
        }

        return builder.toString();
    }

    private static String createRow(
            String[] values,
            int[] widths
    ) {

        return "│ " + // Name
                ConsoleColor.WHITE +
                String.format(
                        "%-" + widths[0] + "s",
                        values[0]
                ) +
                ConsoleColor.RESET +
                " │" + // Version
                " " +
                ConsoleColor.WHITE +
                String.format(
                        "%-" + widths[1] + "s",
                        values[1]
                ) +
                ConsoleColor.RESET +
                " │" + // File
                " " +
                ConsoleColor.WHITE +
                String.format(
                        "%-" + widths[2] + "s",
                        values[2]
                ) +
                ConsoleColor.RESET +
                " │" + // UUID
                " " +
                ConsoleColor.WHITE +
                String.format(
                        "%-" + widths[3] + "s",
                        values[3]
                ) +
                ConsoleColor.RESET +
                " │";
    }

    private static String createRow(
            String[] values,
            int[] widths,
            boolean compatible
    ) {

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

    private static boolean isApiTargetCompatible(String target, String overwriteApiVersion) {
        long apiVersion = versionId(overwriteApiVersion);

        target = target.trim();
        String[] parts = target.split("-");

        if (parts.length == 1) {
            return apiVersion == versionId(parts[0]);
        }

        return versionId(parts[0]) <= apiVersion && apiVersion <= versionId(parts[1]);
    }
}
