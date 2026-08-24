package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.*;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentProvider;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.DynamicCommandBuilder;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Commands.Dynamic.InternalCommandArgument;
import cloud.thehsi.ComitasBotJ.Discord.Commands.Dynamic.InternalCommandArgumentProvider;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.BiConsumer;

public class InternalCommandRegistry implements CommandRegistry {
    final @NotNull List<RegisteredCommand> commands = new ArrayList<>();
    final @NotNull List<RegisteredDynamicCommand> dynamicCommands = new ArrayList<>();

    public InternalCommandRegistry() {}
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".CommandRegistry");

    public void handleCommand(@NotNull SlashCommandInteractionEvent event) {
        for (RegisteredCommand cmd : commands)
            if (Objects.equals(cmd.name(), event.getName())) {
                handleRegisteredCommand(event, cmd);
                return;
            }

        for (RegisteredDynamicCommand cmd : dynamicCommands)
            if (Objects.equals(cmd.name(), event.getName())) {
                handleRegisteredDynamicCommand(event, cmd);
                return;
            }
    }

    private void handleRegisteredCommand(@NotNull SlashCommandInteractionEvent event, @NotNull RegisteredCommand command) {
        Object[] args = new Object[command.method().getParameters().length];
        CommandRanContext commandRanContext = new InternalCommandRanContext(
                event
        );

        List<OptionMapping> options = new ArrayList<>(List.copyOf(event.getOptions()));
        for (int i = 0; i < command.method().getParameters().length; i++) {
            CommandArgument<?> argument = command.arguments()[i];
            if (argument.type() == CommandArgumentType.CONTEXT)
                args[i] = commandRanContext;
            else {
                args[i] = null;
                for (OptionMapping option : options) {
                    if (!option.getName().equals(argument.name())) continue;
                    args[i] = argument.type().converter().apply(option);
                    options.remove(option);
                    break;
                }
            }
        }

        try {
            command.method().invoke(command.commandSupplier(), (Object[]) args);
        } catch (InvocationTargetException e) {
            logger.error("Error whilst executing command {}", commandRanContext.getCommandName(), e.getCause());
        } catch (Exception e) {
            logger.error("Error whilst invoking command {}", commandRanContext.getCommandName(), e);
        }
    }

    private void handleRegisteredDynamicCommand(@NotNull SlashCommandInteractionEvent event, @NotNull RegisteredDynamicCommand command) {
        InternalCommandArgumentProvider provider = new InternalCommandArgumentProvider();

        event.getOptions().forEach(e -> {
            cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType<?> type =
                    commandArgumentTypeFromOptionType(e.getType());

            DynamicCommandBuilder.ICommandArgument argument =
                    command.arguments.stream().filter(arg -> arg.identifier().equals(e.getName())).findFirst().orElse(null);

            if (argument == null)
                return;

            provider.addArgument(
                    e.getName(),
                    new InternalCommandArgument<>(extractValue(e, type), !argument.required(), type)
            );
        });

        CommandRanContext commandRanContext = new InternalCommandRanContext(
                event
        );

        try {
            command.consumer.accept(commandRanContext, provider);
        } catch (Exception e) {
            logger.error("Error whilst invoking command {}", commandRanContext.getCommandName(), e);
        }
    }

    @Override
    public void register(@NotNull CommandSupplier commandSupplier) {
        DebugLogging.action(commandSupplier);
        for (Method method : commandSupplier.getClass().getMethods()) {
            if (!method.isAnnotationPresent(cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command.class)) continue;

            registerMethod(method, commandSupplier);
        }
    }

    @Override
    public void register(@NotNull DynamicCommandBuilder commandBuilder) {
        RegisteredDynamicCommand command = new RegisteredDynamicCommand(
                commandBuilder.getName(),
                commandBuilder.getDescription(),
                commandBuilder.isNsfw(),
                commandBuilder.getCommandTypes(),
                commandBuilder.getCommandContextTypes(),
                commandBuilder.getArguments(),
                commandBuilder.getConsumer()
        );
        register(command);
        dynamicCommands.add(command);
    }

    void registerMethod(@NotNull Method method, @NotNull CommandSupplier commandSupplier) {
        cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command commandInfo = method.getAnnotation(cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command.class);
        CommandArgument<?>[] arguments = new CommandArgument<?>[method.getParameters().length];

        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];

            if (!parameter.isAnnotationPresent(CommandOption.class) && parameter.getType() != CommandRanContext.class)
                throw new RuntimeException("Error whilst loading command " + commandInfo.name() + ", argument " + parameter.getName() + " isn't annotated or Context!");

            if (parameter.getType() == CommandRanContext.class) {
                arguments[i] = new CommandArgument<>(
                        CommandArgumentType.CONTEXT, null, null, true
                );
                continue;
            }

            CommandOption option = parameter.getAnnotation(CommandOption.class);

            if (!Objects.equals(option.name(), option.name().toLowerCase()))
                throw new IllegalArgumentException("Command " + commandInfo.name() + " attempted to register non lowercase argument: " + option.name());

            CommandArgumentType<?> type = CommandArgumentType.fromType(parameter.getType());

            if (type == null)
                throw new RuntimeException("Error whilst loading command " + commandInfo.name() + ", argument " + option.name() + " isn't a supported type!");

            arguments[i] = new CommandArgument<>(type, option.name(), option.description(), option.required());
        }

        RegisteredCommand command = new RegisteredCommand(commandInfo.name(), commandInfo.description(), commandInfo.nsfw(), commandInfo.commandType(), commandInfo.commandContextType(), arguments, method, commandSupplier);

        register(command);

        commands.add(
                command
        );
    }

    public void unregisterAll() {
        if (DiscordAPI.nullableApi() == null)
            throw new IllegalStateException("Command un-registration requested before ready");

        var jda = DiscordAPI.nullableApi();

        List<RestAction<List<Command>>> retrieveActions = new ArrayList<>();
        retrieveActions.add(jda.retrieveCommands());

        for (Guild guild : jda.getGuilds())
            retrieveActions.add(guild.retrieveCommands());

        RestAction.allOf(retrieveActions).flatMap(results -> {
            List<RestAction<Void>> deleteActions = results.stream()
                    .flatMap(Collection::stream)
                    .map(Command::delete)
                    .toList();

            return RestAction.allOf(deleteActions);
        }).complete();

        jda.updateCommands().complete();
        commands.clear();
    }

    void register(@NotNull RegisteredCommand command) {
        if (DiscordAPI.nullableApi() == null) throw new RuntimeException("Command registration requested before ready");
        SlashCommandData data = Commands.slash(command.name(), command.description());

        data = data.setContexts(
                Arrays.stream(command.commandContextTypes())
                        .map(e -> InteractionContextType.fromKey(e.getKey()))
                        .toArray(InteractionContextType[]::new)
        );
        data = data.setIntegrationTypes(
                Arrays.stream(command.commandTypes())
                        .map(e -> IntegrationType.fromKey(e.getKey()))
                        .toArray(IntegrationType[]::new)
        );

        data = data.setNSFW(command.nsfw());

        for (CommandArgument<?> arg : command.arguments())
            if (arg.type() != CommandArgumentType.CONTEXT)
                data = data.addOptions(new OptionData(
                        arg.type().optionType(), arg.name(), arg.description(), arg.required())
                );

        DiscordAPI.nullableApi().upsertCommand(data).queue();
    }

    void register(@NotNull RegisteredDynamicCommand command) {
        if (DiscordAPI.nullableApi() == null) throw new RuntimeException("Command registration requested before ready");
        SlashCommandData data = Commands.slash(command.name(), command.description());

        data = data.setContexts(
                Arrays.stream(command.commandContextTypes())
                        .map(e -> InteractionContextType.fromKey(e.getKey()))
                        .toArray(InteractionContextType[]::new)
        );
        data = data.setIntegrationTypes(
                Arrays.stream(command.commandTypes())
                        .map(e -> IntegrationType.fromKey(e.getKey()))
                        .toArray(IntegrationType[]::new)
        );

        data = data.setNSFW(command.nsfw());


        for (DynamicCommandBuilder.ICommandArgument arg : command.arguments()) {
            data = data.addOptions(new OptionData(
                    optionTypeFromCommandArgumentType(arg.argumentType()),
                    arg.identifier(),
                    arg.description(),
                    arg.required()
            ));
        }

        DiscordAPI.nullableApi().upsertCommand(data).queue();
    }

    @NotNull
    private OptionType optionTypeFromCommandArgumentType(@NotNull cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType<?> type) {
        return switch (type.name()) {
            case "STRING" -> OptionType.STRING;
            case "INTEGER" -> OptionType.INTEGER;
            case "DOUBLE" -> OptionType.NUMBER;
            case "BOOLEAN" -> OptionType.BOOLEAN;
            case "USER" -> OptionType.USER;
            case "CHANNEL" -> OptionType.CHANNEL;
            case "ROLE" -> OptionType.ROLE;
            case "ATTACHMENT" -> OptionType.ATTACHMENT;
            default -> throw new IllegalStateException("Unexpected value: " + type.name());
        };
    }

    @NotNull
    private cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType<?> commandArgumentTypeFromOptionType(@NotNull OptionType type) {
        return switch (type) {
            case STRING -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.STRING;
            case INTEGER -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.INTEGER;
            case NUMBER -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.DOUBLE;
            case BOOLEAN -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.BOOLEAN;
            case USER -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.USER;
            case CHANNEL -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.CHANNEL;
            case ROLE -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.ROLE;
            case ATTACHMENT -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.ATTACHMENT;
            default -> throw new IllegalStateException("Unexpected value: " + type.name());
        };
    }

    @Nullable
    private <T> T extractValue(@NotNull OptionMapping mapping, @NotNull cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType<T> argumentType) {
        return switch (mapping.getType()) {
            case STRING -> argumentType.cast(mapping.getAsString());
            case INTEGER -> argumentType.cast(mapping.getAsInt());
            case NUMBER -> argumentType.cast(mapping.getAsDouble());
            case BOOLEAN -> argumentType.cast(mapping.getAsBoolean());
            case USER -> argumentType.cast(new InternalUser(mapping.getAsUser()));
            case CHANNEL -> argumentType.cast(new InternalChannel(mapping.getAsChannel()));
            case ROLE -> argumentType.cast(new InternalRole(mapping.getAsRole()));
            case ATTACHMENT -> argumentType.cast(new InternalAttachment(mapping.getAsAttachment()));
            default -> throw new IllegalStateException("Unexpected value: " + mapping.getType());
        };
    }

    record RegisteredCommand(String name, String description, boolean nsfw, CommandType[] commandTypes, CommandContextType[] commandContextTypes, CommandArgument<?>[] arguments, Method method, CommandSupplier commandSupplier) {}

    record RegisteredDynamicCommand(String name, String description, boolean nsfw, CommandType[] commandTypes,
                                    CommandContextType[] commandContextTypes,
                                    List<DynamicCommandBuilder.ICommandArgument> arguments,
                                    BiConsumer<CommandRanContext, CommandArgumentProvider> consumer) {
    }
}
