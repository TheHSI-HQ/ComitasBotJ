package cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandContextType;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandType;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows declaring a parent command at runtime, the dynamic counterpart of
 * {@link cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandParent}.
 * <p>
 * A parent is the top level command <code>/name</code> that holds subcommands, which are created
 * using a {@link DynamicCommandBuilder} with {@link DynamicCommandBuilder#setParent(String)} set.
 * Register the parent before its subcommands.
 */
public class CommandParentBuilder {
    @NotNull
    final String name;
    @NotNull
    final String description;
    @NotNull
    final List<Group> groups = new ArrayList<>();
    boolean nsfw = false;
    @NotNull CommandType[] commandTypes = new CommandType[]{CommandType.GUILD_INSTALL};
    @NotNull CommandContextType[] commandContextTypes = new CommandContextType[]{CommandContextType.GUILD};
    @NotNull Permission[] requiredPermissions = new Permission[]{};

    /**
     * Create a new {@link CommandParentBuilder}
     *
     * @param name        The command name, lowercase
     * @param description The command description
     */
    public CommandParentBuilder(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Add a subcommand group (<code>/parent group subcommand</code>) to the parent command
     *
     * @param name        The group name, lowercase
     * @param description The group description
     * @return The builder
     */
    @NotNull
    @Contract("_, _ -> this")
    public CommandParentBuilder addGroup(@NotNull String name, @NotNull String description) {
        this.groups.add(new Group(name, description));
        return this;
    }

    /**
     * @return The command name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * @return The command description
     */
    public @NotNull String getDescription() {
        return description;
    }

    /**
     * @return Is this command, and with it all subcommands, nsfw
     */
    public boolean isNsfw() {
        return nsfw;
    }

    /**
     * @param nsfw Should this command, and with it all subcommands, be nsfw
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public CommandParentBuilder setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
        return this;
    }

    /**
     * @return List the command types
     */
    public @NotNull CommandType[] getCommandTypes() {
        return commandTypes;
    }

    /**
     * @param commandTypes The new command types, applies to all subcommands
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public CommandParentBuilder setCommandTypes(@NotNull CommandType... commandTypes) {
        this.commandTypes = commandTypes;
        return this;
    }

    /**
     * @return List the command context types
     */
    public @NotNull CommandContextType[] getCommandContextTypes() {
        return commandContextTypes;
    }

    /**
     * @param commandContextTypes The new command context types, applies to all subcommands
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public CommandParentBuilder setCommandContextTypes(@NotNull CommandContextType... commandContextTypes) {
        this.commandContextTypes = commandContextTypes;
        return this;
    }

    /**
     * @return List the commands required permissions
     */
    public @NotNull Permission[] getRequiredPermissions() {
        return requiredPermissions;
    }

    /**
     * @param requiredPermissions The commands new required permissions, applies to all subcommands
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public CommandParentBuilder setRequiredPermissions(@NotNull Permission... requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
        return this;
    }

    /**
     * @return List of all subcommand groups
     */
    public @NotNull List<Group> getGroups() {
        return groups;
    }

    /**
     * @param name        The group name
     * @param description The group description
     */
    @ApiStatus.Internal
    public record Group(@NotNull String name, @NotNull String description) {
    }
}
