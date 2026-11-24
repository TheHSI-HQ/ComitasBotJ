package cloud.thehsi.ComitasBotJ.API.Plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A capability, that a sandboxed plugin has to be granted by the server owner, before it can use it.
 * <p>
 * Plugins request permissions in their {@code plugin.properties}:
 * <pre>
 * permissions=network,file_storage
 * permission.network.reason=Fetches the weather from api.example.com
 * permission.file_storage.reason=Caches downloaded images
 * permission.network.required=true
 * </pre>
 * The server owner is asked on first install, which of the requested permissions should be granted.
 * A plugin isn't loaded, if a permission marked as {@code required} isn't granted.
 * The decision is stored in {@code data/security/<Plugin UUID>.security.properties}.
 */
@SuppressWarnings("unused")
public enum PluginPermission {
    /**
     * Allows outgoing network connections (sockets, {@code java.net.http.HttpClient},
     * {@code http(s)://} URLs, ...).
     */
    NETWORK("network", "Network Access"),

    /**
     * Allows writing files inside the plugin's own data folder ({@code data/volumes/<Plugin UUID>/}),
     * see {@link Plugin#getDataFolder()}. Without it, the data folder is read-only.
     */
    FILE_STORAGE("file_storage", "File Storage"),

    /**
     * Allows controlling the whole server: shutting it down ({@link cloud.thehsi.ComitasBotJ.API.Bot.Comitas#shutdown()})
     * and reloading all plugins ({@link PluginManager#reloadSoft()}, {@link PluginManager#reloadHard()}).
     */
    SERVER_CONTROL("server_control", "Server Control (shutdown, reload)"),

    /**
     * Allows loading library jars, which the plugin bundles inside its own jar, <b>without</b> shading them
     * (e.g. an HTTP server). The plugin lists them in its {@code plugin.properties}:
     * <pre>
     * permissions=libraries,network
     * permission.libraries.reason=Serves the dashboard with NanoHTTPD
     * libraries=META-INF/libs/nanohttpd-2.3.1.jar
     * </pre>
     * The library classes are sandboxed like the plugin's own classes: they are verified and only get the
     * permissions the plugin was granted (e.g. a server socket needs {@link #NETWORK}).
     * Libraries that need more than the sandbox permits need {@link #RISKY_LIBRARIES}.
     */
    LIBRARIES("libraries", "Libraries (sandboxed, e.g. HTTP servers)"),

    /**
     * Allows loading libraries, that need more than the sandbox permits, like JDBC drivers with native code
     * (e.g. {@code sqlite-jdbc}). The plugin bundles them as jars inside its own jar and lists them in its
     * {@code plugin.properties}:
     * <pre>
     * permissions=risky_libraries
     * permission.risky_libraries.reason=Stores its data in an SQLite database
     * risky-libraries=META-INF/libs/sqlite-jdbc-3.50.3.0.jar
     * </pre>
     * Library classes are <b>not sandboxed</b>, they have full access to the server (including the bot token).
     * The plugin's own classes stay sandboxed, but may use the library classes and {@code java.sql}.
     * Because of that, this permission is never granted automatically by {@code plugin-permission-default}.
     */
    RISKY_LIBRARIES("risky_libraries", "Risky Libraries (unsandboxed, e.g. database drivers)"),

    /**
     * Allows loading jars from the plugin's file system and running them as Java applications
     * ({@link Plugin#loadJavaApplication(java.nio.file.Path)}), e.g. a packaged tool the plugin downloaded.
     * <p>
     * Applications run inside the plugin's sandbox, with the permissions the plugin was granted. Each application gets
     * its own class loader, so it can't see the plugin's classes and the plugin can't see the application's classes.
     */
    JAVA_APPLICATIONS("java_applications", "Run Java Applications (sandboxed jars)");

    private final @NotNull String key;
    private final @NotNull String displayName;

    PluginPermission(@NotNull String key, @NotNull String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    /**
     * Looks up a permission by its key, as used in {@code plugin.properties}.
     *
     * @param key The permission key, like {@code network}
     * @return The permission or null, if there is no permission with that key
     */
    @Nullable
    public static PluginPermission fromKey(@NotNull String key) {
        String normalized = key.strip().toLowerCase().replace('-', '_');
        for (PluginPermission permission : values())
            if (permission.key.equals(normalized)) return permission;
        return null;
    }

    /**
     * @return The key used in {@code plugin.properties} and {@code .security.properties}
     */
    @NotNull
    public String getKey() {
        return key;
    }

    /**
     * @return A human-readable name of the permission
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }
}
