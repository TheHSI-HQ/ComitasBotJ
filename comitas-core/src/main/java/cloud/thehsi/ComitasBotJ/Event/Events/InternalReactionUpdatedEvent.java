package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.ReactionUpdatedEvent;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.ReactionAction;

public record InternalReactionUpdatedEvent(Member member, Message message,
                                           Reaction reaction, ReactionAction reactionAction) implements ReactionUpdatedEvent {
}
