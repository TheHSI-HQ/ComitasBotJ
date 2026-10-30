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
    FILE_STORAGE("file_storage", "File Storage");

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
