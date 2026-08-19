package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.SelfUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public record InternalBot(SelfUser bot) implements Bot {
    static final Logger debugLogger = DebugLogging.getLogger();

    @Override
    public String getUserName() {
        DebugLogging.action();
        return bot.getName();
    }

    @Override
    public String getDisplayName() {
        DebugLogging.action();
        return bot.getEffectiveName();
    }

    @Override
    public String generateInvitationLink() {
        DebugLogging.action();
        return bot.getJDA().getInviteUrl(Permission.ADMINISTRATOR);
    }

    @Override
    public String generateInvitationLink(cloud.thehsi.ComitasBotJ.API.Discord.Permission... permissions) {
        DebugLogging.action((Object) permissions);
        return bot.getJDA().getInviteUrl(Permission.getPermissions(cloud.thehsi.ComitasBotJ.API.Discord.Permission.asLong(permissions)));
    }

    @Override
    public Long getId() {
        DebugLogging.action();
        return bot.getIdLong();
    }

    @Override
    @ApiStatus.Experimental
    public @Nullable Guild getGuildById(Long id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.Guild guild = bot.getJDA().getGuildById(id);
        if (guild == null) return null;
        return new InternalGuild(guild);
    }

    @Override
    @ApiStatus.Experimental
    public @Nullable Guild getGuildById(String id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.Guild guild = bot.getJDA().getGuildById(id);
        if (guild == null) return null;
        return new InternalGuild(guild);
    }

    @Override
    public List<Guild> getGuilds() {
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
}
