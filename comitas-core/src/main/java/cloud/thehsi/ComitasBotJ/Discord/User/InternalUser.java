package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.atomic.AtomicBoolean;

public class InternalUser implements User {
    private final net.dv8tion.jda.api.entities.User user;
    private final Member member;

    public InternalUser(Member member) {
        this.user = member.getUser();
        this.member = member;
    }

    @Override
    public String getUserName() {
        return user.getName();
    }

    @Override
    public String getDisplayName() {
        return user.getEffectiveName();
    }

    @Override
    public Long getId() {
        return user.getIdLong();
    }

    @Override
    public boolean isBot() {
        return user.isBot();
    }

    @Override
    public boolean isMe() {
        return user.getIdLong() == user.getJDA().getSelfUser().getIdLong();
    }

    @Override
    public String mention() {
        return user.getAsMention();
    }

    @Override
    public boolean sendDirectMessage(String message) {
        AtomicBoolean s = new AtomicBoolean(false);
        member.getUser().openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(message))
                .queue(
                        success -> s.set(true),
                        failure -> s.set(false)
                );
        return s.get();
    }
}
