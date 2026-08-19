package cloud.thehsi.ComitasBotJ.Configuration;

public record StartupProperties(boolean noCmd, boolean ignoreApiTarget, boolean safeMode, boolean strictSafeMode,
                                boolean listPlugins, boolean generateInvite) {
}
