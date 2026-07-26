package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.SelfUser;

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
}
