package cloud.thehsi.ComitasBotJ.Discord.Message.Reaction;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record InternalReaction(MessageReaction reaction, Message message) implements Reaction {
    @Override
    public @NotNull Emoji getEmoji() {
        return new InternalEmoji(reaction.getEmoji());
    }

    @Override
    public @NotNull List<Member> getReactors() {
        DebugLogging.action();

        Guild guild = reaction.getGuild();

        List<Long> userIds = reaction.retrieveUsers()
                .stream()
                .map(User::getIdLong)
                .toList();

        return guild.retrieveMembersByIds(userIds)
                .get()
                .stream()
                .map(InternalMember::new)
                .map(member -> (Member) member)
                .toList();
    }

    @Override
    public int getCount() {
        DebugLogging.action();
        return reaction.getCount();
    }

    @Override
    public boolean haveIReacted() {
        DebugLogging.action();

        return getReactors().stream()
                .anyMatch(e -> e.getId() == reaction.getJDA().getSelfUser().getIdLong());
    }

    @Override
    public boolean exists() {
        DebugLogging.action();
        return reaction.getCount() > 0;
    }

    @Override
    public @NotNull Message getMessage() {
        DebugLogging.action();
        return message;
    }

    @Override
    public void clear() {
        DebugLogging.action();
        reaction.clearReactions().complete();
    }

    @Override
    public void react() {
        DebugLogging.action();
        message.react(new InternalEmoji(reaction.getEmoji()));
    }

    @Override
    public void unreact() {
        DebugLogging.action();
        message.unreact(new InternalEmoji(reaction.getEmoji()));
    }

    @Override
    public void removeReaction(@NotNull Member member) {
        DebugLogging.action(member);
        reaction.removeReaction(((InternalUser) member.getUser()).user).complete();
    }
}
