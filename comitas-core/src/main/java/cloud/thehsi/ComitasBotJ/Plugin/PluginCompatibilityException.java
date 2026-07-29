package cloud.thehsi.ComitasBotJ.Plugin;

public class PluginCompatibilityException extends RuntimeException {
    public PluginCompatibilityException(String targetVersion, String currentVersion) {
        super("Plugin only supports " +
                targetVersion +
                ", current version is " +
                currentVersion);
    }
}
