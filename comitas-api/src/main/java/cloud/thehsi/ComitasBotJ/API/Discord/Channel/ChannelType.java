package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum ChannelType {
    TEXT(TextChannel.class),
    PRIVATE(MessageChannel.class),
    VOICE(TextChannel.class),
    GROUP(MessageChannel.class),
    CATEGORY(Channel.class),
    NEWS(NewsChannel.class),
    STAGE(TextChannel.class),
    GUILD_NEWS_THREAD(ThreadChannel.class),
    GUILD_PUBLIC_THREAD(ThreadChannel.class),
    GUILD_PRIVATE_THREAD(ThreadChannel.class),
    GUILD_DIRECTORY(Channel.class),
    FORUM(ForumChannel.class),
    MEDIA(TextChannel.class),

    UNKNOWN(Channel.class);

    private @NotNull
    final Class<? extends Channel> clazz;

    ChannelType(@NotNull Class<? extends Channel> clazz) {
        this.clazz = clazz;
    }

    @NotNull
    public static ChannelType fromId(@Nullable String id) {
        return Arrays.stream(values())
                .filter(e -> e.name().equals(id))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public boolean isAudio() {
        //noinspection ConstantValue,SwitchStatementWithTooFewBranches
        return switch (this) {
            case VOICE,
                 STAGE -> true;
            default -> false;
        };
    }

    public boolean isThread() {
        return switch (this) {
            case GUILD_NEWS_THREAD,
                 GUILD_PUBLIC_THREAD,
                 GUILD_PRIVATE_THREAD -> true;
            default -> false;
        };
    }

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

    @NotNull
    public Class<? extends Channel> clazz() {
        return clazz;
    }
}
