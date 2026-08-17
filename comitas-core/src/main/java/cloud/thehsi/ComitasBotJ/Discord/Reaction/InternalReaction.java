package cloud.thehsi.ComitasBotJ.Discord.Reaction;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public record InternalReaction(MessageReaction reaction, Message message) implements Reaction {
    @Override
    public Emoji getEmoji() {
        return new InternalEmoji(reaction.getEmoji());
    }

    @Override
    public List<Member> getReactors() {
        Guild guild = reaction.getGuild();

        return reaction.retrieveUsers().stream()
                .map(e -> (Member) new InternalMember(
                        guild.retrieveMember(e).complete()
                ))
                .toList();
    }

    @Override
    public int getCount() {
        return reaction.retrieveUsers().complete().size();
    }

    @Override
    public boolean haveIReacted() {
        for (User user : reaction.retrieveUsers().complete())
            if (user.getIdLong() == reaction.getJDA().getSelfUser().getIdLong())
                return true;

        return false;
    }

    @Override
    public boolean exists() {
        return !reaction.retrieveUsers().complete().isEmpty();
    }

    @Override
    public void clear() {
        reaction.clearReactions().complete();
    }

    @Override
    public void react() {
        message.react(new InternalEmoji(reaction.getEmoji()));
    }

    @Override
    public void unreact() {
        message.unreact(new InternalEmoji(reaction.getEmoji()));
    }

    @Override
    public void removeReaction(Member member) {
        reaction.removeReaction(((InternalUser) member.getUser()).user).complete();
    }
}
