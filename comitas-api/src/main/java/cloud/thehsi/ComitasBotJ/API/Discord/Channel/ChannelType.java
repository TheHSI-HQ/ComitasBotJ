package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * The type of a {@link Channel}
 */
public enum ChannelType {
    /**
     * The Text Chnnel Type
     */
    TEXT(TextChannel.class),
    /**
     * The Private Chnnel Type
     */
    PRIVATE(MessageChannel.class),
    /**
     * The Voice Chnnel Type
     */
    VOICE(VoiceChannel.class),
    /**
     * The Group Chnnel Type
     */
    GROUP(MessageChannel.class),
    /**
     * The Category Chnnel Type
     */
    CATEGORY(Channel.class),
    /**
     * The News Chnnel Type
     */
    NEWS(NewsChannel.class),
    /**
     * The Stage Chnnel Type
     */
    STAGE(VoiceChannel.class),
    /**
     * The Guild News Thread Chnnel Type
     */
    GUILD_NEWS_THREAD(ThreadChannel.class),
    /**
     * The Guild Public Thread Chnnel Type
     */
    GUILD_PUBLIC_THREAD(ThreadChannel.class),
    /**
     * The Guild Private Thread Chnnel Type
     */
    GUILD_PRIVATE_THREAD(ThreadChannel.class),
    /**
     * The Guild Directory Chnnel Type
     */
    GUILD_DIRECTORY(Channel.class),
    /**
     * The Forum Chnnel Type
     */
    FORUM(ForumChannel.class),
    /**
     * The Media Chnnel Type
     */
    MEDIA(TextChannel.class),

    /**
     * The Unknown Chnnel Type
     */
    UNKNOWN(Channel.class);

    private @NotNull
    final Class<? extends Channel> clazz;

    ChannelType(@NotNull Class<? extends Channel> clazz) {
        this.clazz = clazz;
    }

    /**
     * Resolve a channel type id to a channel type
     *
     * @param id The id to resolve
     * @return The resolved channel or UNKNOWN
     */
    @NotNull
    public static ChannelType fromId(@Nullable String id) {
        return Arrays.stream(values())
                .filter(e -> e.name().equals(id))
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * @return Does this channel type support audio
     */
    public boolean isAudio() {
        //noinspection ConstantValue,SwitchStatementWithTooFewBranches
        return switch (this) {
            case VOICE,
                 STAGE -> true;
            default -> false;
        };
    }

    /**
     * @return Is this channel type a thread
     */
    public boolean isThread() {
        return switch (this) {
            case GUILD_NEWS_THREAD,
                 GUILD_PUBLIC_THREAD,
                 GUILD_PRIVATE_THREAD -> true;
            default -> false;
        };
    }

    /**
     * @return Does this channel type support messages
     */
    public boolean isMessage() {
        return switch (this) {
            case TEXT,
                 VOICE,
                 STAGE,
                 NEWS,
                 PRIVATE,
                 GROUP,
                 GUILD_NEWS_THREAD,
                 GUILD_PUBLIC_THREAD,
                 GUILD_PRIVATE_THREAD -> true;
            default -> false;
        };
    }

    /**
     * @return Get the class for this {@link ChannelType}
     */
    @NotNull
    public Class<? extends Channel> clazz() {
        return clazz;
    }
}
