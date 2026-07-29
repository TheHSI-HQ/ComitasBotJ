package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.SelfUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record InternalBot(SelfUser bot) implements Bot {
    @Override
    public String getUserName() {
        return bot.getName();
    }

    @Override
    public String getDisplayName() {
        return bot.getEffectiveName();
    }

    @Override
    public String generateInvitationLink() {
        return bot.getJDA().getInviteUrl(Permission.ADMINISTRATOR);
    }

    @Override
    public Long getId() {
        return bot.getIdLong();
    }

    @Override
    @ApiStatus.Experimental
    public @Nullable Guild getGuildById(Long id) {
        net.dv8tion.jda.api.entities.Guild guild = bot.getJDA().getGuildById(id);
        if (guild == null) return null;
        return new InternalGuild(guild);
    }

    @Override
    @ApiStatus.Experimental
    public @Nullable Guild getGuildById(String id) {
        net.dv8tion.jda.api.entities.Guild guild = bot.getJDA().getGuildById(id);
        if (guild == null) return null;
        return new InternalGuild(guild);
    }

    @Override
    public List<Guild> getGuilds() {
        return bot.getJDA().getGuilds().stream().map(e -> (Guild) new InternalGuild(e)).toList();
    }

    @Override
    public boolean isMe(@Nullable Member member) {
        if (member == null) return false;
        else return member.isMe();
    }

    @Override
    public boolean isMeOrNull(@Nullable Member member) {
        if (member == null) return true;
        else return member.isMe();
    }
}
