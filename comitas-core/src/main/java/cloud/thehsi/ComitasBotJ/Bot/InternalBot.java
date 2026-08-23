package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.Activity;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.ActivityType;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.OnlineStatus;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotUpdatePresenceEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalBotUpdatePresenceEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.SelfUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public record InternalBot(SelfUser bot, EventManager eventManager) implements Bot {
    @NotNull
    static final Logger debugLogger = DebugLogging.getLogger();

    @Override
    public @NotNull String getUserName() {
        DebugLogging.action();
        return bot.getName();
    }

    @Override
    public @NotNull String getDisplayName() {
        DebugLogging.action();
        return bot.getEffectiveName();
    }

    @Override
    public @NotNull String generateInvitationLink() {
        DebugLogging.action();
        return bot.getJDA().getInviteUrl(Permission.ADMINISTRATOR);
    }

    @Override
    public @NotNull String generateInvitationLink(@NotNull cloud.thehsi.ComitasBotJ.API.Discord.Permission... permissions) {
        DebugLogging.action((Object) permissions);
        return bot.getJDA().getInviteUrl(Permission.getPermissions(cloud.thehsi.ComitasBotJ.API.Discord.Permission.asLong(permissions)));
    }

    @Override
    public long getId() {
        DebugLogging.action();
        return bot.getIdLong();
    }

    @Override
    @ApiStatus.Experimental
    public @Nullable Guild getGuildById(@NotNull Long id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.Guild guild = bot.getJDA().getGuildById(id);
        if (guild == null) return null;
        return new InternalGuild(guild);
    }

    @Override
    @ApiStatus.Experimental
    public @Nullable Guild getGuildById(@NotNull String id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.Guild guild = bot.getJDA().getGuildById(id);
        if (guild == null) return null;
        return new InternalGuild(guild);
    }

    @Override
    public @NotNull List<Guild> getGuilds() {
        DebugLogging.action();
        return bot.getJDA().getGuilds().stream().map(e -> (Guild) new InternalGuild(e)).toList();
    }

    @Override
    public boolean isMe(@Nullable Member member) {
        DebugLogging.action(member);
        if (member == null) return false;
        else return member.isMe();
    }

    @Override
    public boolean isMeOrNull(@Nullable Member member) {
        DebugLogging.action(member);
        if (member == null) return true;
        else return member.isMe();
    }

    @Override
    public @Nullable Activity getActivity() {
        DebugLogging.action();
        net.dv8tion.jda.api.entities.Activity activity = bot.getJDA().getPresence().getActivity();
        if (activity == null)
            return null;
        return Activity.of(
                ActivityType.fromKey(activity.getType().getKey()),
                activity.getName(),
                activity.getUrl()
        );
    }

    @Override
    public void setActivity(@Nullable Activity activity) {
        DebugLogging.action(activity);
        Activity oldActivity = getActivity();

        if (activity == null)
            bot.getJDA().getPresence().setActivity(null);
        else
            bot.getJDA().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(
                    net.dv8tion.jda.api.entities.Activity.ActivityType.fromKey(activity.getType().getKey()),
                    activity.getName(),
                    activity.getUrl()
            ));

        eventManager().runCallbackAsync(
                new InternalBotUpdatePresenceEvent(
                        oldActivity,
                        activity,
                        null,
                        getOnlineStatus(),
                        this,
                        EventManager.resolveUndoLoopPrevention(BotUpdatePresenceEvent.class,
                                true, activity == null ? null : activity.getName(), activity == null ? null : activity.getUrl()
                        )
                ),
                event -> {
                    if (event.willUndo()) {
                        EventManager.addUndoLoopPrevention(BotUpdatePresenceEvent.class,
                                true, oldActivity == null ? null : oldActivity.getName(), oldActivity == null ? null : oldActivity.getUrl()
                        );

                        setActivity(oldActivity);
                    }
                }
        );
    }

    @Override
    public @NotNull OnlineStatus getOnlineStatus() {
        DebugLogging.action();
        return OnlineStatus.fromKey(bot.getJDA().getPresence().getStatus().getKey());
    }

    @Override
    public void setOnlineStatus(@NotNull OnlineStatus onlineStatus) {
        DebugLogging.action(onlineStatus);

        OnlineStatus oldStatus = getOnlineStatus();

        bot.getJDA().getPresence().setStatus(net.dv8tion.jda.api.OnlineStatus.fromKey(onlineStatus.getKey()));

        eventManager().runCallbackAsync(
                new InternalBotUpdatePresenceEvent(
                        null,
                        getActivity(),
                        oldStatus,
                        getOnlineStatus(),
                        this,
                        EventManager.resolveUndoLoopPrevention(BotUpdatePresenceEvent.class,
                                false, onlineStatus.getKey()
                        )
                ),
                event -> {
                    if (event.willUndo()) {
                        EventManager.addUndoLoopPrevention(BotUpdatePresenceEvent.class,
                                false, oldStatus.getKey()
                        );

                        setOnlineStatus(oldStatus);
                    }
                }
        );
    }
}
