package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.InteractionContext;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
abstract public class InternalInteractionContext implements InteractionContext {
    final User user;
    final @Nullable Channel channel;
    final @Nullable Guild guild;
    final Interaction interaction;

    public InternalInteractionContext(Interaction interaction) {
        this.interaction = interaction;

        this.user = new InternalUser(interaction.getUser());
        this.channel = interaction.getChannel() == null ?
                null : new InternalChannel(interaction.getChannel());
        this.guild = interaction.getGuild() == null ?
                null : new InternalGuild(interaction.getGuild());
    }

    @Override
    abstract public void acknowledge();

    @Override
    public User getUser() {
        DebugLogging.action();
        return user;
    }

    @Override
    public @Nullable Channel getChannel() {
        DebugLogging.action();
        return channel;
    }

    @Override
    public @Nullable Guild getGuild() {
        DebugLogging.action();
        return guild;
    }
}
