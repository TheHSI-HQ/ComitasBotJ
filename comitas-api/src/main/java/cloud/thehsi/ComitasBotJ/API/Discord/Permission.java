package cloud.thehsi.ComitasBotJ.API.Discord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class Permission {
    private static final Logger logger = LoggerFactory.getLogger("ComitasBotJ.API.Permission");
    public static final Permission CREATE_INSTANT_INVITE =
            new Permission(1L, "CREATE_INSTANT_INVITE");
    public static final Permission KICK_MEMBERS =
            new Permission(2L << 1, "KICK_MEMBERS");
    public static final Permission BAN_MEMBERS =
            new Permission(2L << 2, "BAN_MEMBERS");
    public static final Permission ADMINISTRATOR =
            new Permission(2L << 3, "ADMINISTRATOR");
    public static final Permission MANAGE_CHANNEL =
            new Permission(2L << 4, "MANAGE_CHANNEL");
    public static final Permission MANAGE_GUILD =
            new Permission(2L << 5, "MANAGE_GUILD");
    public static final Permission ADD_REACTIONS =
            new Permission(2L << 6, "ADD_REACTIONS");
    public static final Permission VIEW_AUDIT_LOGS =
            new Permission(2L << 7, "VIEW_AUDIT_LOGS");
    public static final Permission PRIORITY_SPEAKER =
            new Permission(2L << 8, "PRIORITY_SPEAKER");
    public static final Permission STREAM =
            new Permission(2L << 9, "STREAM");
    public static final Permission VIEW_CHANNEL =
            new Permission(2L << 10, "VIEW_CHANNEL");
    public static final Permission SEND_MESSAGES =
            new Permission(2L << 11, "SEND_MESSAGES");
    public static final Permission SEND_TTS_MESSAGES =
            new Permission(2L << 12, "SEND_TTS_MESSAGES");
    public static final Permission MANAGE_MESSAGES =
            new Permission(2L << 13, "MANAGE_MESSAGES");
    public static final Permission EMBED_LINKS =
            new Permission(2L << 14, "EMBED_LINKS");
    public static final Permission ATTACH_FILES =
            new Permission(2L << 15, "ATTACH_FILES");
    public static final Permission READ_MESSAGE_HISTORY =
            new Permission(2L << 16, "READ_MESSAGE_HISTORY");
    public static final Permission MENTION_EVERYONE =
            new Permission(2L << 17, "MENTION_EVERYONE");
    public static final Permission USE_EXTERNAL_EMOJIS =
            new Permission(2L << 18, "USE_EXTERNAL_EMOJIS");
    public static final Permission VIEW_GUILD_INSIGHTS =
            new Permission(2L << 19, "VIEW_GUILD_INSIGHTS");
    public static final Permission CONNECT =
            new Permission(2L << 20, "CONNECT");
    public static final Permission SPEAK =
            new Permission(2L << 21, "SPEAK");
    public static final Permission MUTE_MEMBERS =
            new Permission(2L << 22, "MUTE_MEMBERS");
    public static final Permission DEAFEN_MEMBERS =
            new Permission(2L << 23, "DEAFEN_MEMBERS");
    public static final Permission MOVE_MEMBERS =
            new Permission(2L << 24, "MOVE_MEMBERS");
    public static final Permission USE_VAD =
            new Permission(2L << 25, "USE_VAD");
    public static final Permission CHANGE_NICKNAME =
            new Permission(2L << 26, "CHANGE_NICKNAME");
    public static final Permission MANAGE_NICKNAMES =
            new Permission(2L << 27, "MANAGE_NICKNAMES");
    public static final Permission MANAGE_ROLES =
            new Permission(2L << 28, "MANAGE_ROLES");
    public static final Permission MANAGE_WEBHOOKS =
            new Permission(2L << 29, "MANAGE_WEBHOOKS");
    public static final Permission MANAGE_GUILD_EXPRESSIONS =
            new Permission(2L << 30, "MANAGE_GUILD_EXPRESSIONS");
    public static final Permission USE_APPLICATION_COMMANDS =
            new Permission(2L << 31, "USE_APPLICATION_COMMANDS");
    public static final Permission REQUEST_TO_SPEAK =
            new Permission(2L << 32, "REQUEST_TO_SPEAK");
    public static final Permission MANAGE_EVENTS =
            new Permission(2L << 33, "MANAGE_EVENTS");
    public static final Permission MANAGE_THREADS =
            new Permission(2L << 34, "MANAGE_THREADS");
    public static final Permission CREATE_PUBLIC_THREADS =
            new Permission(2L << 35, "CREATE_PUBLIC_THREADS");
    public static final Permission CREATE_PRIVATE_THREADS =
            new Permission(2L << 36, "CREATE_PRIVATE_THREADS");
    public static final Permission USE_EXTERNAL_STICKERS =
            new Permission(2L << 37, "USE_EXTERNAL_STICKERS");
    public static final Permission SEND_MESSAGES_IN_THREADS =
            new Permission(2L << 38, "SEND_MESSAGES_IN_THREADS");
    public static final Permission USE_EMBEDDED_ACTIVITIES =
            new Permission(2L << 39, "USE_EMBEDDED_ACTIVITIES");
    public static final Permission MODERATE_MEMBERS =
            new Permission(2L << 40, "MODERATE_MEMBERS");
    public static final Permission VIEW_CREATOR_MONETIZATION_ANALYTICS =
            new Permission(2L << 41, "VIEW_CREATOR_MONETIZATION_ANALYTICS");
    public static final Permission USE_SOUNDBOARD =
            new Permission(2L << 42, "USE_SOUNDBOARD");
    public static final Permission CREATE_GUILD_EXPRESSIONS =
            new Permission(2L << 43, "CREATE_GUILD_EXPRESSIONS");
    public static final Permission CREATE_EVENTS =
            new Permission(2L << 44, "CREATE_EVENTS");
    public static final Permission USE_EXTERNAL_SOUNDS =
            new Permission(2L << 45, "USE_EXTERNAL_SOUNDS");
    public static final Permission SEND_VOICE_MESSAGES =
            new Permission(2L << 46, "SEND_VOICE_MESSAGES");
    public static final Permission SET_VOICE_CHANNEL_STATUS =
            new Permission(2L << 48, "SET_VOICE_CHANNEL_STATUS");
    public static final Permission SEND_POLLS =
            new Permission(2L << 49, "SEND_POLLS");
    public static final Permission USE_EXTERNAL_APPS =
            new Permission(2L << 50, "USE_EXTERNAL_APPS");
    public static final Permission PIN_MESSAGES =
            new Permission(2L << 51, "PIN_MESSAGES");
    public static final Permission BYPASS_SLOWMODE =
            new Permission(2L << 52, "BYPASS_SLOWMODE");

    public static Permission fromValue(String permission) {
        return switch (permission) {
            case "CREATE_INSTANT_INVITE" -> CREATE_INSTANT_INVITE;
            case "KICK_MEMBERS" -> KICK_MEMBERS;
            case "BAN_MEMBERS" -> BAN_MEMBERS;
            case "ADMINISTRATOR" -> ADMINISTRATOR;
            case "MANAGE_CHANNEL" -> MANAGE_CHANNEL;
            case "MANAGE_SERVER", "MANAGE_GUILD" -> MANAGE_GUILD;
            case "ADD_REACTIONS", "MESSAGE_ADD_REACTION" -> ADD_REACTIONS;
            case "VIEW_AUDIT_LOGS" -> VIEW_AUDIT_LOGS;
            case "PRIORITY_SPEAKER" -> PRIORITY_SPEAKER;
            case "STREAM", "VOICE_STREAM" -> STREAM;
            case "VIEW_CHANNEL" -> VIEW_CHANNEL;
            case "SEND_MESSAGES", "MESSAGE_SEND" -> SEND_MESSAGES;
            case "SEND_TTS_MESSAGES", "MESSAGE_TTS" -> SEND_TTS_MESSAGES;
            case "MANAGE_MESSAGES", "MESSAGE_MANAGE" -> MANAGE_MESSAGES;
            case "EMBED_LINKS", "MESSAGE_EMBED_LINKS" -> EMBED_LINKS;
            case "ATTACH_FILES", "MESSAGE_ATTACH_FILES" -> ATTACH_FILES;
            case "READ_MESSAGE_HISTORY", "MESSAGE_HISTORY" -> READ_MESSAGE_HISTORY;
            case "MENTION_EVERYONE", "MESSAGE_MENTION_EVERYONE" -> MENTION_EVERYONE;
            case "USE_EXTERNAL_EMOJIS", "MESSAGE_EXT_EMOJI" -> USE_EXTERNAL_EMOJIS;
            case "VIEW_GUILD_INSIGHTS" -> VIEW_GUILD_INSIGHTS;
            case "CONNECT", "VOICE_CONNECT" -> CONNECT;
            case "SPEAK", "VOICE_SPEAK" -> SPEAK;
            case "MUTE_MEMBERS", "VOICE_MUTE_OTHERS" -> MUTE_MEMBERS;
            case "DEAFEN_MEMBERS", "VOICE_DEAF_OTHERS" -> DEAFEN_MEMBERS;
            case "MOVE_MEMBERS", "VOICE_MOVE_OTHERS" -> MOVE_MEMBERS;
            case "USE_VAD", "VOICE_USE_VAD" -> USE_VAD;
            case "CHANGE_NICKNAME", "NICKNAME_CHANGE" -> CHANGE_NICKNAME;
            case "MANAGE_NICKNAMES", "NICKNAME_MANAGE" -> MANAGE_NICKNAMES;
            case "MANAGE_ROLES", "MANAGE_PERMISSIONS" -> MANAGE_ROLES;
            case "MANAGE_WEBHOOKS" -> MANAGE_WEBHOOKS;
            case "MANAGE_GUILD_EXPRESSIONS" -> MANAGE_GUILD_EXPRESSIONS;
            case "USE_APPLICATION_COMMANDS" -> USE_APPLICATION_COMMANDS;
            case "REQUEST_TO_SPEAK" -> REQUEST_TO_SPEAK;
            case "MANAGE_EVENTS" -> MANAGE_EVENTS;
            case "MANAGE_THREADS" -> MANAGE_THREADS;
            case "CREATE_PUBLIC_THREADS" -> CREATE_PUBLIC_THREADS;
            case "CREATE_PRIVATE_THREADS" -> CREATE_PRIVATE_THREADS;
            case "USE_EXTERNAL_STICKERS", "MESSAGE_EXT_STICKER" -> USE_EXTERNAL_STICKERS;
            case "SEND_MESSAGES_IN_THREADS", "MESSAGE_SEND_IN_THREADS" -> SEND_MESSAGES_IN_THREADS;
            case "USE_EMBEDDED_ACTIVITIES" -> USE_EMBEDDED_ACTIVITIES;
            case "MODERATE_MEMBERS" -> MODERATE_MEMBERS;
            case "VIEW_CREATOR_MONETIZATION_ANALYTICS" -> VIEW_CREATOR_MONETIZATION_ANALYTICS;
            case "USE_SOUNDBOARD", "VOICE_USE_SOUNDBOARD" -> USE_SOUNDBOARD;
            case "CREATE_GUILD_EXPRESSIONS" -> CREATE_GUILD_EXPRESSIONS;
            case "CREATE_EVENTS", "CREATE_SCHEDULED_EVENTS" -> CREATE_EVENTS;
            case "USE_EXTERNAL_SOUNDS", "VOICE_USE_EXTERNAL_SOUNDS" -> USE_EXTERNAL_SOUNDS;
            case "SEND_VOICE_MESSAGES", "MESSAGE_ATTACH_VOICE_MESSAGE" -> SEND_VOICE_MESSAGES;
            case "SET_VOICE_CHANNEL_STATUS", "VOICE_SET_STATUS" -> SET_VOICE_CHANNEL_STATUS;
            case "SEND_POLLS", "MESSAGE_SEND_POLLS" -> SEND_POLLS;
            case "USE_EXTERNAL_APPS", "USE_EXTERNAL_APPLICATIONS" -> USE_EXTERNAL_APPS;
            case "PIN_MESSAGES" -> PIN_MESSAGES;
            case "BYPASS_SLOWMODE" -> BYPASS_SLOWMODE;
            default -> {
                logger.warn("Unexpected value: {}", permission);
                yield null;
            }
        };
    }

    final long permission;
    final String name;

    private Permission(long permission, String name) {
        this.permission = permission;
        this.name = name;
    }
}
