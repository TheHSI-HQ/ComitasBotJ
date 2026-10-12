package cloud.thehsi.ComitasBotJ.API.Discord;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Represents a discord permission
 */
@SuppressWarnings({"unused"})
public enum Permission {
    /**
     * The Create Instant Invite permission
     */
    CREATE_INSTANT_INVITE(1L, "CREATE_INSTANT_INVITE"),
    /**
     * The Kick Members permission
     */
    KICK_MEMBERS(2L, "KICK_MEMBERS"),
    /**
     * The Ban Members permission
     */
    BAN_MEMBERS(2L << 1, "BAN_MEMBERS"),
    /**
     * The Administrator permission
     */
    ADMINISTRATOR(2L << 2, "ADMINISTRATOR"),
    /**
     * The Manage Channel permission
     */
    MANAGE_CHANNEL(2L << 3, "MANAGE_CHANNEL"),
    /**
     * The Manage Guild permission
     */
    MANAGE_GUILD(2L << 4, "MANAGE_SERVER", "MANAGE_GUILD"),
    /**
     * The Add Reactions permission
     */
    ADD_REACTIONS(2L << 5, "ADD_REACTIONS", "MESSAGE_ADD_REACTION"),
    /**
     * The View Audit Logs permission
     */
    VIEW_AUDIT_LOGS(2L << 6, "VIEW_AUDIT_LOGS"),
    /**
     * The Priority Speaker permission
     */
    PRIORITY_SPEAKER(2L << 7, "PRIORITY_SPEAKER"),
    /**
     * The Stream permission
     */
    STREAM(2L << 8, "STREAM", "VOICE_STREAM"),
    /**
     * The View Channel permission
     */
    VIEW_CHANNEL(2L << 9, "VIEW_CHANNEL"),
    /**
     * The Send Messages permission
     */
    SEND_MESSAGES(2L << 10, "SEND_MESSAGES", "MESSAGE_SEND"),
    /**
     * The Send Tts Messages permission
     */
    SEND_TTS_MESSAGES(2L << 11, "SEND_TTS_MESSAGES", "MESSAGE_TTS"),
    /**
     * The Manage Messages permission
     */
    MANAGE_MESSAGES(2L << 12, "MANAGE_MESSAGES", "MESSAGE_MANAGE"),
    /**
     * The Embed Links permission
     */
    EMBED_LINKS(2L << 13, "EMBED_LINKS", "MESSAGE_EMBED_LINKS"),
    /**
     * The Attach Files permission
     */
    ATTACH_FILES(2L << 14, "ATTACH_FILES", "MESSAGE_ATTACH_FILES"),
    /**
     * The Read Message History permission
     */
    READ_MESSAGE_HISTORY(2L << 15, "READ_MESSAGE_HISTORY", "MESSAGE_HISTORY"),
    /**
     * The Mention Everyone permission
     */
    MENTION_EVERYONE(2L << 16, "MENTION_EVERYONE", "MESSAGE_MENTION_EVERYONE"),
    /**
     * The Use External Emojis permission
     */
    USE_EXTERNAL_EMOJIS(2L << 17, "USE_EXTERNAL_EMOJIS", "MESSAGE_EXT_EMOJI"),
    /**
     * The View Guild Insights permission
     */
    VIEW_GUILD_INSIGHTS(2L << 18, "VIEW_GUILD_INSIGHTS"),
    /**
     * The Connect permission
     */
    CONNECT(2L << 19, "CONNECT", "VOICE_CONNECT"),
    /**
     * The Stream permission
     */
    SPEAK(2L << 20, "SPEAK", "VOICE_SPEAK"),
    /**
     * The Mute Members permission
     */
    MUTE_MEMBERS(2L << 21, "MUTE_MEMBERS", "VOICE_MUTE_OTHERS"),
    /**
     * The Deafen Members permission
     */
    DEAFEN_MEMBERS(2L << 22, "DEAFEN_MEMBERS", "VOICE_DEAF_OTHERS"),
    /**
     * The Move Members permission
     */
    MOVE_MEMBERS(2L << 23, "MOVE_MEMBERS", "VOICE_MOVE_OTHERS"),
    /**
     * The Use Vad permission
     */
    USE_VAD(2L << 24, "USE_VAD", "VOICE_USE_VAD"),
    /**
     * The Change Nickname permission
     */
    CHANGE_NICKNAME(2L << 25, "CHANGE_NICKNAME", "NICKNAME_CHANGE"),
    /**
     * The Manage Nicknames permission
     */
    MANAGE_NICKNAMES(2L << 26, "MANAGE_NICKNAMES", "NICKNAME_MANAGE"),
    /**
     * The Manage Roles permission
     */
    MANAGE_ROLES(2L << 27, "MANAGE_ROLES", "MANAGE_PERMISSIONS"),
    /**
     * The Manage Webhooks permission
     */
    MANAGE_WEBHOOKS(2L << 28, "MANAGE_WEBHOOKS"),
    /**
     * The Manage Guild Expressions permission
     */
    MANAGE_GUILD_EXPRESSIONS(2L << 29, "MANAGE_GUILD_EXPRESSIONS"),
    /**
     * The Use Application Commands permission
     */
    USE_APPLICATION_COMMANDS(2L << 30, "USE_APPLICATION_COMMANDS"),
    /**
     * The Request To Speak permission
     */
    REQUEST_TO_SPEAK(2L << 31, "REQUEST_TO_SPEAK"),
    /**
     * The Manage Events permission
     */
    MANAGE_EVENTS(2L << 32, "MANAGE_EVENTS"),
    /**
     * The Manage Threads permission
     */
    MANAGE_THREADS(2L << 33, "MANAGE_THREADS"),
    /**
     * The Create Public Threads permission
     */
    CREATE_PUBLIC_THREADS(2L << 34, "CREATE_PUBLIC_THREADS"),
    /**
     * The Create Private Threads permission
     */
    CREATE_PRIVATE_THREADS(2L << 35, "CREATE_PRIVATE_THREADS"),
    /**
     * The Use External Stickers permission
     */
    USE_EXTERNAL_STICKERS(2L << 36, "USE_EXTERNAL_STICKERS", "MESSAGE_EXT_STICKER"),
    /**
     * The Send Messages In Threads permission
     */
    SEND_MESSAGES_IN_THREADS(2L << 37, "SEND_MESSAGES_IN_THREADS", "MESSAGE_SEND_IN_THREADS"),
    /**
     * The Use Embedded Activities permission
     */
    USE_EMBEDDED_ACTIVITIES(2L << 38, "USE_EMBEDDED_ACTIVITIES"),
    /**
     * The Moderate Members permission
     */
    MODERATE_MEMBERS(2L << 39, "MODERATE_MEMBERS"),
    /**
     * The View Creator Monetization Analytics permission
     */
    VIEW_CREATOR_MONETIZATION_ANALYTICS(2L << 40, "VIEW_CREATOR_MONETIZATION_ANALYTICS"),
    /**
     * The Use Soundboard permission
     */
    USE_SOUNDBOARD(2L << 41, "USE_SOUNDBOARD", "VOICE_USE_SOUNDBOARD"),
    /**
     * The Create Guild Expressions permission
     */
    CREATE_GUILD_EXPRESSIONS(2L << 42, "CREATE_GUILD_EXPRESSIONS"),
    /**
     * The Create Events permission
     */
    CREATE_EVENTS(2L << 43, "CREATE_EVENTS", "CREATE_SCHEDULED_EVENTS"),
    /**
     * The Use External Sounds permission
     */
    USE_EXTERNAL_SOUNDS(2L << 44, "USE_EXTERNAL_SOUNDS", "VOICE_USE_EXTERNAL_SOUNDS"),
    /**
     * The Send Voice Messages permission
     */
    SEND_VOICE_MESSAGES(2L << 45, "SEND_VOICE_MESSAGES", "MESSAGE_ATTACH_VOICE_MESSAGE"),
    /**
     * The Set Voice Channel Status permission
     */
    SET_VOICE_CHANNEL_STATUS(2L << 46, "SET_VOICE_CHANNEL_STATUS", "VOICE_SET_STATUS"),
    /**
     * The Send Polls permission
     */
    SEND_POLLS(2L << 47, "SEND_POLLS", "MESSAGE_SEND_POLLS"),
    /**
     * The Use External Apps permission
     */
    USE_EXTERNAL_APPS(2L << 48, "USE_EXTERNAL_APPS", "USE_EXTERNAL_APPLICATIONS"),
    /**
     * The Pin Messages permission
     */
    PIN_MESSAGES(2L << 49, "PIN_MESSAGES"),
    /**
     * The Bypass Slowmode permission
     */
    BYPASS_SLOWMODE(2L << 50, "BYPASS_SLOWMODE");

    private @NotNull
    static final Logger logger = LoggerFactory.getLogger("ComitasBotJ.API.Permission");
    final long permission;
    final @NotNull String[] names;

    Permission(long permission, @NotNull String... names) {
        this.permission = permission;
        this.names = names;
    }

    /**
     * Resolve a permission from an internal identifier
     *
     * @param permission The internal identifier
     * @return The resolved permission or null
     */
    @Nullable
    public static Permission fromValue(@NotNull String permission) {
        return Arrays.stream(values())
                .filter(e -> Arrays.asList(e.names).contains(permission))
                .findFirst()
                .orElseGet(() -> {
                    logger.warn("Unexpected value: {}", permission);
                    return null;
                });
    }

    /**
     * Converts an array of permissions to a permission number
     *
     * @param permissions A list of permissions
     * @return The permissions represented as a permission number
     */
    public static long asLong(@NotNull Permission... permissions) {
        return Arrays.stream(permissions)
                .mapToLong(e -> e.permission)
                .reduce(0L, (a, b) -> a | b);
    }

    /**
     * Converts a permission number to an array of permissions
     *
     * @param permission A permission number
     * @return The generated array of permissions
     */
    @NotNull
    public static Permission[] fromLong(long permission) {
        return Arrays.stream(values())
                .filter(e -> (permission & e.permission) != 0)
                .toArray(Permission[]::new);
    }

    @Override
    @NotNull
    public String toString() {
        return "Permission{" +
                "name=" + name() +
                '}';
    }
}
