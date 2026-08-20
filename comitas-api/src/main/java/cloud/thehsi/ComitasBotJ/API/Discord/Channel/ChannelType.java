package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import java.util.Arrays;

public enum ChannelType {
    MESSAGE(MessageChannel.class),
    FORUM(ForumChannel.class),
    THREAD(ThreadChannel.class),
    GUILD_NEWS_THREAD(ThreadChannel.class),
    GUILD_PUBLIC_THREAD(ThreadChannel.class),
    GUILD_PRIVATE_THREAD(ThreadChannel.class),
    GUILD_DIRECTORY(Channel.class),
    UNKNOWN(Channel.class);

    private final Class<? extends Channel> clazz;

    ChannelType(Class<? extends Channel> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Channel> clazz() {
        return clazz;
    }

    public boolean isAudio() {
        //noinspection ConstantValue,SwitchStatementWithTooFewBranches
        return switch (this) {
            default -> false;
        };
    }

    public boolean isThread() {
        return switch (this) {
            case THREAD,
                 GUILD_NEWS_THREAD,
                 GUILD_PUBLIC_THREAD,
                 GUILD_PRIVATE_THREAD -> true;
            default -> false;
        };
    }

    public boolean isMessage() {
        return switch (this) {
            case MESSAGE,
                 FORUM,
                 THREAD,
                 GUILD_NEWS_THREAD,
                 GUILD_PUBLIC_THREAD,
                 GUILD_PRIVATE_THREAD -> true;
            default -> false;
        };
    }

    public static ChannelType fromId(String id) {
        return Arrays.stream(values())
                .filter(e -> e.name().equals(id))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
