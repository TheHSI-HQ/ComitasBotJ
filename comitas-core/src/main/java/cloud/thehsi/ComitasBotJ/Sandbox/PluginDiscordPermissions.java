package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.Discord.BotPermissionSnapshot;
import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The Discord permissions a plugin declares in its {@code plugin.properties}:
 * <pre>
 * discord-permissions=send_messages,embed_links,manage_messages
 * discord-permission.send_messages.required=true
 * discord-permission.manage_messages.reason=Deletes spam
 * </pre>
 * <ul>
 *     <li>A {@code required} (hard) permission must be granted by the server owner, and the bot must have it
 *     in at least one guild, otherwise the plugin isn't loaded.</li>
 *     <li>Other (soft) permissions only cause a warning, if they are missing.</li>
 *     <li>The server owner decides, which of them the plugin may use. The plugin can only call API methods,
 *     whose {@link cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission} are satisfied by the granted ones,
 *     no matter which permissions the bot itself has.</li>
 * </ul>
 * Keys are {@link Permission} names, case insensitive, {@code -} may be used instead of {@code _}.
 */
public final class PluginDiscordPermissions {
    static final String LIST_KEY = "discord-permissions";
    static final String KEY_PREFIX = "discord-permission.";
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Sandbox");

    private PluginDiscordPermissions() {
    }

    /**
     * A Discord permission, declared in a plugin.properties.
     *
     * @param reason   Why the plugin needs it, shown to the server owner
     * @param required If the plugin can't work without it (hard dependency)
     */
    public record Request(@NotNull Permission permission, @Nullable String reason, boolean required) {
    }

    /**
     * @param permission A Discord permission
     * @return The key used in plugin.properties and the security file, like {@code send_messages}
     */
    @NotNull
    public static String key(@NotNull Permission permission) {
        return permission.name().toLowerCase(Locale.ROOT);
    }

    /**
     * @param key A key, like {@code send_messages} or {@code SEND-MESSAGES}
     * @return The permission, or null if there is none with that name
     */
    @Nullable
    public static Permission fromKey(@NotNull String key) {
        String normalized = key.strip().toUpperCase(Locale.ROOT).replace('-', '_');
        for (Permission permission : Permission.values())
            if (permission.name().equals(normalized)) return permission;
        return null;
    }

    /**
     * @return True if the plugin declares its Discord permissions (even an empty list), so it is limited to them
     */
    public static boolean declares(@NotNull Properties props) {
        if (props.containsKey(LIST_KEY)) return true;
        return props.stringPropertyNames().stream().anyMatch(key -> key.startsWith(KEY_PREFIX) && key.endsWith(".required"));
    }

    /**
     * @return The declared Discord permissions, in declaration order
     */
    @NotNull
    public static LinkedHashMap<Permission, Request> parseRequests(@NotNull Properties props, @NotNull String pluginName) {
        Set<Permission> requested = new LinkedHashSet<>();
        for (String key : props.getProperty(LIST_KEY, "").split("[,\\s]+")) {
            if (key.isBlank()) continue;
            Permission permission = fromKey(key);
            if (permission == null) {
                logger.warn("Plugin {} declares the unknown Discord permission '{}', it is ignored", pluginName, key);
                continue;
            }
            requested.add(permission);
        }
        // A required permission is requested, even if it is missing in the list
        for (Permission permission : Permission.values())
            if (PluginSecurity.parseBoolean(props.getProperty(KEY_PREFIX + key(permission) + ".required", "false")))
                requested.add(permission);

        LinkedHashMap<Permission, Request> requests = new LinkedHashMap<>();
        for (Permission permission : requested) {
            String reason = props.getProperty(KEY_PREFIX + key(permission) + ".reason");
            boolean required = PluginSecurity.parseBoolean(props.getProperty(KEY_PREFIX + key(permission) + ".required", "false"));
            requests.put(permission, new Request(permission, reason == null || reason.isBlank() ? null : reason.strip(), required));
        }
        return requests;
    }

    // --- Bot permissions ---

    /**
     * How well the bot's permissions cover the ones a plugin declares.
     *
     * @param missingRequired Required permissions, the bot has in no guild, the plugin must not be loaded
     * @param missingOptional Optional permissions, the bot has in no guild
     * @param lackingGuilds   Declared permissions, which the bot has in some guilds only, with the guilds lacking them
     */
    public record BotCheck(@NotNull @Unmodifiable List<Permission> missingRequired,
                           @NotNull @Unmodifiable List<Permission> missingOptional,
                           @NotNull @Unmodifiable Map<Permission, List<String>> lackingGuilds) {
        public boolean refuses() {
            return !missingRequired.isEmpty();
        }
    }

    /**
     * @param requests The plugin's declared permissions
     * @param bot      The bot's current permissions
     */
    @NotNull
    public static BotCheck check(@NotNull List<Request> requests, @NotNull BotPermissionSnapshot bot) {
        List<Permission> missingRequired = new ArrayList<>();
        List<Permission> missingOptional = new ArrayList<>();
        Map<Permission, List<String>> lackingGuilds = new LinkedHashMap<>();
        for (Request request : requests) {
            Permission permission = request.permission();
            if (!bot.grantedAnywhere(permission)) {
                (request.required() ? missingRequired : missingOptional).add(permission);
                continue;
            }
            List<String> lacking = bot.guildsLacking(permission);
            if (!lacking.isEmpty()) lackingGuilds.put(permission, lacking);
        }
        return new BotCheck(List.copyOf(missingRequired), List.copyOf(missingOptional), Map.copyOf(lackingGuilds));
    }

    // --- Runtime checks of conditional permissions ---

    /**
     * Checks the content dependent permissions of a message, the calling plugin sends or edits in a guild.
     * Calls of the base plugin and Comitas itself aren't limited.
     *
     * @throws SandboxViolationException If the calling plugin wasn't granted a permission the content needs
     */
    public static void requireForContent(@NotNull MessageData messageData) {
        PluginSandbox sandbox = PluginSandbox.calling();
        if (sandbox == null || sandbox.discordGranted() == null) return;

        Set<Permission> needed = EnumSet.noneOf(Permission.class);
        if (!messageData.getEmbeds().isEmpty()) needed.add(Permission.EMBED_LINKS);
        if (!messageData.getAttachmentUploads().isEmpty() || !messageData.getAttachments().isEmpty())
            needed.add(Permission.ATTACH_FILES);
        if (messageData.getAttachmentUploads().stream().anyMatch(AttachmentUpload::isVoiceMessage))
            needed.add(Permission.SEND_VOICE_MESSAGES);

        List<String> missing = needed.stream().filter(permission -> !sandbox.allowsDiscord(permission))
                .map(PluginDiscordPermissions::key).toList();
        if (!missing.isEmpty())
            throw SandboxRuntime.violation("Plugin " + sandbox.pluginName() + ": the message requires the Discord permission(s) ["
                    + String.join(", ", missing) + "], which weren't granted to the plugin");
    }
}
