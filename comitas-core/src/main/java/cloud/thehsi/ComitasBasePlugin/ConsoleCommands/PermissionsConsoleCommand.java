package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import cloud.thehsi.ComitasBotJ.Configuration.DataPaths;
import cloud.thehsi.ComitasBotJ.Sandbox.PluginSecurity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class PermissionsConsoleCommand extends ConsoleCommandExecutor {
    private static final String SUFFIX = ".security.properties";

    public PermissionsConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Lists the permissions and mount points of a plugin",
                "permissions", "perms"
        );
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length == 0) {
            listKnownPlugins();
            return;
        }

        UUID uuid = resolve(String.join(" ", args));
        if (uuid == null) {
            getConsoleLogger().error("Unknown plugin '{}', use a uuid or the name of a loaded plugin. Usage: permissions [uuid]",
                    String.join(" ", args));
            return;
        }

        try {
            show(uuid);
        } catch (IOException e) {
            getConsoleLogger().error("Couldn't read {}: {}", DataPaths.securityFile(uuid), e.getLocalizedMessage());
        }
    }

    /**
     * Accepts a uuid or the name of a loaded plugin.
     */
    @Nullable
    private static UUID resolve(@NotNull String argument) {
        try {
            return UUID.fromString(argument.strip());
        } catch (IllegalArgumentException ignored) {
            // Not a uuid, try the name
        }
        return Comitas.getPluginManager().getAllPluginMetadata().stream()
                .filter(metadata -> metadata.name().equalsIgnoreCase(argument.strip()))
                .map(Plugin.PluginMetadata::uuid)
                .findFirst().orElse(null);
    }

    @Nullable
    private static Plugin.PluginMetadata loaded(@NotNull UUID uuid) {
        return Comitas.getPluginManager().getAllPluginMetadata().stream()
                .filter(metadata -> metadata.uuid().equals(uuid))
                .findFirst().orElse(null);
    }

    private void show(@NotNull UUID uuid) throws IOException {
        Plugin.PluginMetadata metadata = loaded(uuid);
        PluginSecurity.Settings settings = PluginSecurity.readSettings(uuid);

        getConsoleLogger().info("{}{}{} ({}) - {}", ConsoleColor.BOLD,
                metadata == null ? "Unknown plugin" : metadata.name() + " " + metadata.version(), ConsoleColor.RESET,
                uuid, metadata == null ? "not loaded" : "loaded");
        getConsoleLogger().info("  Settings: {}", DataPaths.securityFile(uuid));
        getConsoleLogger().info("  Files:    {} (seen as /)", DataPaths.volume(uuid));

        if (settings == null) {
            getConsoleLogger().info("  No security settings yet, the plugin hasn't been loaded with this uuid.");
            return;
        }

        getConsoleLogger().info("Permissions:");
        for (PluginPermission permission : PluginPermission.values()) {
            Boolean granted = settings.permissions().get(permission);
            String state = granted == null ? ConsoleColor.BRIGHT_BLACK + "undecided"
                    : granted ? ConsoleColor.BRIGHT_GREEN + "granted" : ConsoleColor.BRIGHT_RED + "denied";
            getConsoleLogger().info("  {}{} ({}): {}{}", ConsoleColor.WHITE, permission.getDisplayName(),
                    permission.getKey(), state, ConsoleColor.RESET);
        }

        getConsoleLogger().info("Mount points:");
        if (settings.mounts().isEmpty()) getConsoleLogger().info("  none");
        for (PluginSecurity.ConfiguredMount mount : settings.mounts()) {
            getConsoleLogger().info("  /mnt/{} -> {}{}{}", mount.name(), mount.directory(),
                    mount.readOnly() ? " (read-only)" : "",
                    mount.problem() == null ? "" : ConsoleColor.BRIGHT_RED + " - unavailable: " + mount.problem() + ConsoleColor.RESET);
        }
        if (!settings.deniedMounts().isEmpty())
            getConsoleLogger().info("Never granted: {}", String.join(", ", settings.deniedMounts().stream().map(name -> "/mnt/" + name).toList()));

        if (metadata != null) getConsoleLogger().info("Changes to the settings file apply after a reload.");
    }

    private void listKnownPlugins() {
        List<Path> files = List.of();
        if (Files.isDirectory(DataPaths.SECURITY)) {
            try (Stream<Path> paths = Files.list(DataPaths.SECURITY)) {
                files = paths.filter(path -> path.getFileName().toString().endsWith(SUFFIX)).sorted().toList();
            } catch (IOException e) {
                getConsoleLogger().error("Couldn't list {}: {}", DataPaths.SECURITY, e.getLocalizedMessage());
            }
        }

        getConsoleLogger().info("Usage: permissions <uuid | plugin name>");
        if (files.isEmpty()) {
            getConsoleLogger().info("No plugin security settings exist yet.");
            return;
        }

        getConsoleLogger().info("Plugins with security settings:");
        for (Path file : files) {
            String name = file.getFileName().toString();
            String uuid = name.substring(0, name.length() - SUFFIX.length());
            Plugin.PluginMetadata metadata;
            try {
                metadata = loaded(UUID.fromString(uuid));
            } catch (IllegalArgumentException e) {
                metadata = null;
            }
            getConsoleLogger().info("  {} {}", uuid, metadata == null ? ConsoleColor.BRIGHT_BLACK + "(not loaded)" + ConsoleColor.RESET
                    : ConsoleColor.WHITE + metadata.name() + " " + metadata.version() + ConsoleColor.RESET);
        }
    }
}
