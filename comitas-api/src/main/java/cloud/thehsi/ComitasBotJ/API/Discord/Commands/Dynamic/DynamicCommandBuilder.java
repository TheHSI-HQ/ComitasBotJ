package cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandContextType;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRanContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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

    public DynamicCommandBuilder(@NotNull String name, @NotNull String description, @NotNull BiConsumer<CommandRanContext, CommandArgumentProvider> consumer) {
        this.name = name;
        this.consumer = consumer;
        this.description = description;
    }

    @NotNull
    public DynamicCommandBuilder addArgument(@NotNull String identifier, @NotNull String description, @NotNull CommandArgumentType<?> commandArgumentType) {
        return addArgument(identifier, description, true, commandArgumentType);
    }

    @NotNull
    public DynamicCommandBuilder addArgument(@NotNull String identifier, @NotNull String description, boolean required, @NotNull CommandArgumentType<?> commandArgumentType) {
        this.arguments.add(
                new ICommandArgument(identifier, description, required, commandArgumentType)
        );
        return this;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public @NotNull CommandType[] getCommandTypes() {
        return commandTypes;
    }

    @NotNull
    public DynamicCommandBuilder setCommandTypes(@NotNull CommandType... commandTypes) {
        this.commandTypes = commandTypes;
        return this;
    }

    public @NotNull CommandContextType[] getCommandContextTypes() {
        return commandContextTypes;
    }

    @NotNull
    public DynamicCommandBuilder setCommandContextTypes(@NotNull CommandContextType... commandContextTypes) {
        this.commandContextTypes = commandContextTypes;
        return this;
    }

    public @NotNull List<ICommandArgument> getArguments() {
        return arguments;
    }

    public @NotNull BiConsumer<CommandRanContext, CommandArgumentProvider> getConsumer() {
        return consumer;
    }

    public record ICommandArgument(@NotNull String identifier, @NotNull String description, boolean required,
                                   @NotNull CommandArgumentType<?> argumentType) {
    }
}
