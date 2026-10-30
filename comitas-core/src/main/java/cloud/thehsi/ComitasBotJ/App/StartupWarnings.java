package cloud.thehsi.ComitasBotJ.App;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.Configuration.StartupProperties;
import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class StartupWarnings {
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH);

    public static void log(@NotNull StartupProperties props, @NotNull ServerConfig.ParsedServerConfig conf) {
        if (props.quiet()) return;

        if (Objects.equals(conf.allowedPlugins.get(), "*"))
            warn("""
                    Plugin whitelist is not enabled.
                    
                    For production environments, enabling the plugin whitelist is
                    strongly recommended to prevent unexpected or untrusted plugins
                    from being loaded.
                    """);

        if (props.ignoreApiTarget())
            warn("""
                    Plugin compatibility checks are disabled.
                    
                    ComitasBotJ will load plugins regardless of their compatibility
                    status. This feature is intended for development and testing only
                    and must not be used in production environments.
                    
                    Proceed with caution.
                    """);

        if (props.safeMode() && !props.strictSafeMode())
            warn("""
                    Safe Mode is enabled.
                    
                    ComitasBotJ will not load any plugins.
                    This mode is intended only for debugging and testing
                    and should not be used in production environments.
                    """);

        if (props.strictSafeMode())
            warn("""
                    Strict Safe Mode is enabled.
                    
                    Plugin loading and all built-in commands are disabled.
                    To quit ComitasBotJ, press Ctrl+C (^C).
                    
                    This mode is intended only for debugging and testing
                    and should not be used in production environments.
                    """);
    }

    private static void warn(@NotNull String message) {
        logger.warn("{}\n{}", ConsoleColor.YELLOW, message);
    }
}
