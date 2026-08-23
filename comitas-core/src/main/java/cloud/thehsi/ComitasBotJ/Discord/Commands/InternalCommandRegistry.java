package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.*;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class InternalCommandRegistry implements CommandRegistry {
    final @NotNull List<RegisteredCommand> commands = new ArrayList<>();

    public InternalCommandRegistry() {}
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".CommandRegistry");

    public void handleCommand(@NotNull SlashCommandInteractionEvent event) {
        RegisteredCommand command = null;

        for (RegisteredCommand cmd : commands)
            if (Objects.equals(cmd.name(), event.getName())) {
                command = cmd;
                break;
            }

        if (command == null) return;

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

    @Override
    public void register(@NotNull CommandSupplier commandSupplier) {
        DebugLogging.action(commandSupplier);
        for (Method method : commandSupplier.getClass().getMethods()) {
            if (!method.isAnnotationPresent(cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command.class)) continue;

            registerMethod(method, commandSupplier);
        }
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

    record RegisteredCommand(String name, String description, boolean nsfw, CommandType[] commandTypes, CommandContextType[] commandContextTypes, CommandArgument<?>[] arguments, Method method, CommandSupplier commandSupplier) {}
}
