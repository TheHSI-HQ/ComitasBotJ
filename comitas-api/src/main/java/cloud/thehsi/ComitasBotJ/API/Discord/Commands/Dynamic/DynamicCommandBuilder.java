package cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandContextType;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRanContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Allows creation of commands at runtime
 */
public class DynamicCommandBuilder {
    @NotNull
    final String name;
    @NotNull
    final String description;
    @NotNull
    final List<ICommandArgument> arguments = new ArrayList<>();
    @NotNull
    final BiConsumer<CommandRanContext, CommandArgumentProvider> consumer;
    boolean nsfw = false;
    @NotNull CommandType[] commandTypes = new CommandType[]{CommandType.GUILD_INSTALL};
    @NotNull CommandContextType[] commandContextTypes = new CommandContextType[]{CommandContextType.GUILD};

    /**
     * Create a new {@link DynamicCommandBuilder}
     *
     * @param name        The command name
     * @param description The command description
     * @param consumer    A {@link BiConsumer} taking a {@link CommandRanContext} and {@link CommandArgumentProvider}, for handeling the command execution
     */
    public DynamicCommandBuilder(@NotNull String name, @NotNull String description, @NotNull BiConsumer<CommandRanContext, CommandArgumentProvider> consumer) {
        this.name = name;
        this.consumer = consumer;
        this.description = description;
    }

    /**
     * Add a required argument to the dynamic command builder
     *
     * @param identifier The argument identifier
     * @param description The argument description
     * @param commandArgumentType The argument type
     * @return The builder
     */
    @NotNull
    @Contract("_, _, _ -> this")
    public DynamicCommandBuilder addArgument(@NotNull String identifier, @NotNull String description, @NotNull CommandArgumentType<?> commandArgumentType) {
        return addArgument(identifier, description, true, commandArgumentType);
    }

    /**
     * Add a required argument to the dynamic command builder
     *
     * @param identifier The argument identifier
     * @param description The argument description
     * @param required Is this argument required
     * @param commandArgumentType The argument type
     * @return The builder
     */
    @NotNull
    @Contract("_, _, _, _ -> this")
    public DynamicCommandBuilder addArgument(@NotNull String identifier, @NotNull String description, boolean required, @NotNull CommandArgumentType<?> commandArgumentType) {
        this.arguments.add(
                new ICommandArgument(identifier, description, required, commandArgumentType)
        );
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
     * @return Is this command nsfw
     */
    public boolean isNsfw() {
        return nsfw;
    }

    /**
     * @param nsfw Should this command be nsfw
     */
    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    /**
     * @return List the command types
     */
    public @NotNull CommandType[] getCommandTypes() {
        return commandTypes;
    }

    /**
     * @param commandTypes The new command types
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public DynamicCommandBuilder setCommandTypes(@NotNull CommandType... commandTypes) {
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
     * @param commandContextTypes The new command context types
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public DynamicCommandBuilder setCommandContextTypes(@NotNull CommandContextType... commandContextTypes) {
        this.commandContextTypes = commandContextTypes;
        return this;
    }

    /**
     * @return List of all command arguments
     */
    public @NotNull List<ICommandArgument> getArguments() {
        return arguments;
    }

    /**
     * @return  The command consumer
     */
    public @NotNull BiConsumer<CommandRanContext, CommandArgumentProvider> getConsumer() {
        return consumer;
    }

    /**
     * @param identifier   The argument identifier
     * @param description  The argument description
     * @param required     Is the argumnt required
     * @param argumentType The argument type
     */
    @ApiStatus.Internal
    public record ICommandArgument(@NotNull String identifier, @NotNull String description, boolean required,
                                   @NotNull CommandArgumentType<?> argumentType) {
    }
}
