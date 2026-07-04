package cloud.thehsi.ComitasBotJ.Discord.Reaction;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import net.dv8tion.jda.api.entities.MessageReaction;

public class InternalReaction implements Reaction {
    MessageReaction reaction;
    Message message;

    public InternalReaction(MessageReaction reaction, Message message) {
        this.reaction = reaction;
        this.message = message;
    }

    @Override
    public Emoji getEmoji() {
        return new InternalEmoji(reaction.getEmoji());
    }

    @Override
    public int getCount() {
        return reaction.getCount();
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public void clear() {
        reaction.clearReactions().queue();
    }
}
