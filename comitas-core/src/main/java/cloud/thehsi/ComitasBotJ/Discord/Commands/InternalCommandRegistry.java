package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.*;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InternalCommandRegistry implements CommandRegistry {
    private final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".CommandRegistry");
    private DiscordAPI api;

    public InternalCommandRegistry() {}

    final List<RegisteredCommand> commands = new ArrayList<>();

    public void handleCommand(SlashCommandInteractionEvent event) {
        RegisteredCommand command = null;

        for (RegisteredCommand cmd : commands)
            if (Objects.equals(cmd.name(), event.getName())) {
                command = cmd;
                break;
            }

        if (command == null) return;

        Object[] args = new Object[command.method().getParameters().length];
        Context context = new InternalContext(
                event
        );

        List<OptionMapping> options = new ArrayList<>(List.copyOf(event.getOptions()));
        for (int i = 0; i < command.method().getParameters().length; i++) {
            CommandArgument<?> argument = command.arguments()[i];
            if (argument.type() == CommandArgumentType.CONTEXT)
                args[i] = context;
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
            logger.error("Error whilst executing command {}", context.commandName(), e.getCause());
        } catch (Exception e) {
            logger.error("Error whilst invoking command {}", context.commandName(), e);
        }
    }

    @Override
    public void register(CommandSupplier commandSupplier) {
        for (Method method : commandSupplier.getClass().getMethods()) {
            if (!method.isAnnotationPresent(cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command.class)) continue;

            registerMethod(method, commandSupplier);
        }
    }

    void registerMethod(Method method, CommandSupplier commandSupplier) {
        cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command commandInfo = method.getAnnotation(cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command.class);
        CommandArgument<?>[] arguments = new CommandArgument<?>[method.getParameters().length];

        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];

            if (!parameter.isAnnotationPresent(CommandOption.class) && parameter.getType() != Context.class)
                throw new RuntimeException("Error whilst loading command " + commandInfo.name() + ", argument " + parameter.getName() + " isn't annotated or Context!");

            if (parameter.getType() == Context.class) {
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

        RegisteredCommand command = new RegisteredCommand(commandInfo.name(), commandInfo.description(), arguments, method, commandSupplier);

        register(command);

        commands.add(
                command
        );
    }

    public void setDiscordApi(DiscordAPI api) {
        if (this.api != null) return;

        this.api = api;
    }

    public void unregisterAll() {
        if (this.api == null) throw new RuntimeException("Command registration requested before ready");

        for (Command command : api.getAPI().retrieveCommands().complete())
            command.delete().complete();
        api.getAPI().updateCommands().complete();
    }

    void register(RegisteredCommand command) {
        if (this.api == null) throw new RuntimeException("Command registration requested before ready");
        SlashCommandData data = Commands.slash(command.name(), command.description());

        data = data.setContexts(InteractionContextType.PRIVATE_CHANNEL, InteractionContextType.BOT_DM, InteractionContextType.GUILD);

        for (CommandArgument<?> arg : command.arguments())
            if (arg.type() != CommandArgumentType.CONTEXT)
                data = data.addOptions(new OptionData(
                        arg.type().optionType(), arg.name(), arg.description(), arg.required())
                );

        this.api.getAPI().updateCommands()
                .addCommands(data)
                .complete();
    }

    record RegisteredCommand(String name, String description, CommandArgument<?>[] arguments, Method method, CommandSupplier commandSupplier) {}
}
