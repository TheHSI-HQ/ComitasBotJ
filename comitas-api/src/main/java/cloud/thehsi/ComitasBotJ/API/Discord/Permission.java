package cloud.thehsi.ComitasBotJ.API.Discord;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@SuppressWarnings({"unused"})
public enum Permission {
    CREATE_INSTANT_INVITE(1L, "CREATE_INSTANT_INVITE"),
    KICK_MEMBERS(2L, "KICK_MEMBERS"),
    BAN_MEMBERS(2L << 1, "BAN_MEMBERS"),
    ADMINISTRATOR(2L << 2, "ADMINISTRATOR"),
    MANAGE_CHANNEL(2L << 3, "MANAGE_CHANNEL"),
    MANAGE_GUILD(2L << 4, "MANAGE_SERVER", "MANAGE_GUILD"),
    ADD_REACTIONS(2L << 5, "ADD_REACTIONS", "MESSAGE_ADD_REACTION"),
    VIEW_AUDIT_LOGS(2L << 6, "VIEW_AUDIT_LOGS"),
    PRIORITY_SPEAKER(2L << 7, "PRIORITY_SPEAKER"),
    STREAM(2L << 8, "STREAM", "VOICE_STREAM"),
    VIEW_CHANNEL(2L << 9, "VIEW_CHANNEL"),
    SEND_MESSAGES(2L << 10, "SEND_MESSAGES", "MESSAGE_SEND"),
    SEND_TTS_MESSAGES(2L << 11, "SEND_TTS_MESSAGES", "MESSAGE_TTS"),
    MANAGE_MESSAGES(2L << 12, "MANAGE_MESSAGES", "MESSAGE_MANAGE"),
    EMBED_LINKS(2L << 13, "EMBED_LINKS", "MESSAGE_EMBED_LINKS"),
    ATTACH_FILES(2L << 14, "ATTACH_FILES", "MESSAGE_ATTACH_FILES"),
    READ_MESSAGE_HISTORY(2L << 15, "READ_MESSAGE_HISTORY", "MESSAGE_HISTORY"),
    MENTION_EVERYONE(2L << 16, "MENTION_EVERYONE", "MESSAGE_MENTION_EVERYONE"),
    USE_EXTERNAL_EMOJIS(2L << 17, "USE_EXTERNAL_EMOJIS", "MESSAGE_EXT_EMOJI"),
    VIEW_GUILD_INSIGHTS(2L << 18, "VIEW_GUILD_INSIGHTS"),
    CONNECT(2L << 19, "CONNECT", "VOICE_CONNECT"),
    SPEAK(2L << 20, "SPEAK", "VOICE_SPEAK"),
    MUTE_MEMBERS(2L << 21, "MUTE_MEMBERS", "VOICE_MUTE_OTHERS"),
    DEAFEN_MEMBERS(2L << 22, "DEAFEN_MEMBERS", "VOICE_DEAF_OTHERS"),
    MOVE_MEMBERS(2L << 23, "MOVE_MEMBERS", "VOICE_MOVE_OTHERS"),
    USE_VAD(2L << 24, "USE_VAD", "VOICE_USE_VAD"),
    CHANGE_NICKNAME(2L << 25, "CHANGE_NICKNAME", "NICKNAME_CHANGE"),
    MANAGE_NICKNAMES(2L << 26, "MANAGE_NICKNAMES", "NICKNAME_MANAGE"),
    MANAGE_ROLES(2L << 27, "MANAGE_ROLES", "MANAGE_PERMISSIONS"),
    MANAGE_WEBHOOKS(2L << 28, "MANAGE_WEBHOOKS"),
    MANAGE_GUILD_EXPRESSIONS(2L << 29, "MANAGE_GUILD_EXPRESSIONS"),
    USE_APPLICATION_COMMANDS(2L << 30, "USE_APPLICATION_COMMANDS"),
    REQUEST_TO_SPEAK(2L << 31, "REQUEST_TO_SPEAK"),
    MANAGE_EVENTS(2L << 32, "MANAGE_EVENTS"),
    MANAGE_THREADS(2L << 33, "MANAGE_THREADS"),
    CREATE_PUBLIC_THREADS(2L << 34, "CREATE_PUBLIC_THREADS"),
    CREATE_PRIVATE_THREADS(2L << 35, "CREATE_PRIVATE_THREADS"),
    USE_EXTERNAL_STICKERS(2L << 36, "USE_EXTERNAL_STICKERS", "MESSAGE_EXT_STICKER"),
    SEND_MESSAGES_IN_THREADS(2L << 37, "SEND_MESSAGES_IN_THREADS", "MESSAGE_SEND_IN_THREADS"),
    USE_EMBEDDED_ACTIVITIES(2L << 38, "USE_EMBEDDED_ACTIVITIES"),
    MODERATE_MEMBERS(2L << 39, "MODERATE_MEMBERS"),
    VIEW_CREATOR_MONETIZATION_ANALYTICS(2L << 40, "VIEW_CREATOR_MONETIZATION_ANALYTICS"),
    USE_SOUNDBOARD(2L << 41, "USE_SOUNDBOARD", "VOICE_USE_SOUNDBOARD"),
    CREATE_GUILD_EXPRESSIONS(2L << 42, "CREATE_GUILD_EXPRESSIONS"),
    CREATE_EVENTS(2L << 43, "CREATE_EVENTS", "CREATE_SCHEDULED_EVENTS"),
    USE_EXTERNAL_SOUNDS(2L << 44, "USE_EXTERNAL_SOUNDS", "VOICE_USE_EXTERNAL_SOUNDS"),
    SEND_VOICE_MESSAGES(2L << 45, "SEND_VOICE_MESSAGES", "MESSAGE_ATTACH_VOICE_MESSAGE"),
    SET_VOICE_CHANNEL_STATUS(2L << 46, "SET_VOICE_CHANNEL_STATUS", "VOICE_SET_STATUS"),
    SEND_POLLS(2L << 47, "SEND_POLLS", "MESSAGE_SEND_POLLS"),
    USE_EXTERNAL_APPS(2L << 48, "USE_EXTERNAL_APPS", "USE_EXTERNAL_APPLICATIONS"),
    PIN_MESSAGES(2L << 49, "PIN_MESSAGES"),
    BYPASS_SLOWMODE(2L << 50, "BYPASS_SLOWMODE");

    private @NotNull
    static final Logger logger = LoggerFactory.getLogger("ComitasBotJ.API.Permission");
    final long permission;
    final @NotNull String[] names;

    Permission(long permission, @NotNull String... names) {
        this.permission = permission;
        this.names = names;
    }

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

    public static long asLong(@NotNull Permission[] permissions) {
        return Arrays.stream(permissions).mapToLong(e -> e.permission).sum();
    }

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
