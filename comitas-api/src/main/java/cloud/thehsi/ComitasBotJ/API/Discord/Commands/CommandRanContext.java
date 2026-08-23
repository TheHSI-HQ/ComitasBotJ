package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Interaction.RepliableInteraction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface CommandRanContext extends RepliableInteraction {
    /**
     * The sender of the command
     *
     * @return The sender of the command
     */
    @Nullable
    Member getSender();

    /**
     * The channel in which the command was run
     *
     * @return The channel in which the command was run
     */
    @Override
    @NotNull
    MessageChannel getChannel();

    /**
     * The name of the command
     *
     * @return The name of the command
     */
    @NotNull
    String getCommandName();
}
