package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Interaction.RepliableInteraction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context for when a command is run
 */
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
     * The name of the command, for subcommands this is the name of the top level (parent) command
     *
     * @return The name of the command
     */
    @NotNull
    String getCommandName();

    /**
     * The name of the subcommand which was run, for <code>/parent group sub</code> and <code>/parent sub</code>
     * this is <code>sub</code>
     *
     * @return The name of the subcommand, or null if a regular command was run
     */
    @Nullable
    String getSubcommandName();

    /**
     * The name of the subcommand group of the subcommand which was run, for <code>/parent group sub</code>
     * this is <code>group</code>
     *
     * @return The name of the subcommand group, or null if the command isn't a subcommand or isn't in a group
     */
    @Nullable
    String getSubcommandGroup();
}
