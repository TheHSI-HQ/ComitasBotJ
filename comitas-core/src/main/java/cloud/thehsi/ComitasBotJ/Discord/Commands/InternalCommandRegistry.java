package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.*;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentProvider;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandParentBuilder;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.DynamicCommandBuilder;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.ChannelTypeResolver;
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
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class InternalCommandRegistry implements CommandRegistry {
    final @NotNull List<RegisteredCommand> commands = new CopyOnWriteArrayList<>();
    final @NotNull List<RegisteredDynamicCommand> dynamicCommands = new CopyOnWriteArrayList<>();
    /**
     * Declared parent commands by name, they hold the subcommands in {@link #commands} and {@link #dynamicCommands}
     */
    final @NotNull Map<String, RegisteredParent> parents = new ConcurrentHashMap<>();
    final @NotNull Logger debugLogger = DebugLogging.getLogger();

    public InternalCommandRegistry() {}
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".CommandRegistry");
    private static final @NotNull CommandType[] DEFAULT_COMMAND_TYPES = {CommandType.GUILD_INSTALL};
    private static final @NotNull CommandContextType[] DEFAULT_COMMAND_CONTEXT_TYPES = {CommandContextType.GUILD};

    public void handleCommand(@NotNull SlashCommandInteractionEvent event) {
        for (RegisteredCommand cmd : commands)
            if (matches(cmd.name(), cmd.parent(), cmd.group(), event)) {
                handleRegisteredCommand(event, cmd);
                return;
            }

        for (RegisteredDynamicCommand cmd : dynamicCommands)
            if (matches(cmd.name(), cmd.parent(), cmd.group(), event)) {
                handleRegisteredDynamicCommand(event, cmd);
                return;
            }
    }

    /**
     * Does a registered command, which is a subcommand if it has a parent, belong to the ran command
     */
    private static boolean matches(@NotNull String name, @Nullable String parent, @Nullable String group, @NotNull SlashCommandInteractionEvent event) {
        if (parent == null)
            return Objects.equals(name, event.getName()) && event.getSubcommandName() == null;

        return Objects.equals(parent, event.getName())
                && Objects.equals(name, event.getSubcommandName())
                && Objects.equals(group, event.getSubcommandGroup());
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
                    if (argument.type().converter() == null) continue;
                    try {
                        args[i] = argument.type().converter().apply(option);
                    } catch (Exception e) {
                        logger.error("There was an error whilst parsing option '{}' of command '{}'", option.getName(), event.getFullCommandName(), e);
                    }
                    options.remove(option);
                    break;
                }
            }
        }

        try {
            command.method().invoke(command.commandSupplier(), (Object[]) args);
        } catch (InvocationTargetException e) {
            logger.error("Error whilst executing command {}", event.getFullCommandName(), e.getCause());
        } catch (Exception e) {
            logger.error("Error whilst invoking command {}", event.getFullCommandName(), e);
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

            try {
                provider.addArgument(
                        e.getName(),
                        new InternalCommandArgument<>(extractValue(e, type), !argument.required(), type)
                );
            } catch (Exception ex) {
                logger.error("There was an error whilst parsing option '{}' of command '{}'", argument.identifier(), event.getFullCommandName(), ex);
            }
        });

        CommandRanContext commandRanContext = new InternalCommandRanContext(
                event
        );

        try {
            command.consumer.accept(commandRanContext, provider);
        } catch (Exception e) {
            logger.error("Error whilst invoking command {}", event.getFullCommandName(), e);
        }
    }

    @Override
    public void register(@NotNull CommandSupplier commandSupplier) {
        DebugLogging.action(commandSupplier);

        Set<String> changedParents = new LinkedHashSet<>();
        syncAfter(changedParents, () -> {
            // Parents come first, so the subcommands of this supplier can reference them
            for (CommandParent parent : commandSupplier.getClass().getAnnotationsByType(CommandParent.class))
                declareParent(RegisteredParent.of(parent));

            for (Method method : commandSupplier.getClass().getMethods()) {
                if (!method.isAnnotationPresent(cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command.class)) continue;

                registerMethod(method, commandSupplier, changedParents);
            }
        });
    }

    @Override
    public void register(@NotNull DynamicCommandBuilder commandBuilder) {
        String parent = blankToNull(commandBuilder.getParent());
        String group = blankToNull(commandBuilder.getGroup());
        if (parent == null && group != null)
            throw new IllegalArgumentException("Command " + commandBuilder.getName() + " sets the group '" + group + "', but has no parent");

        RegisteredDynamicCommand command = new RegisteredDynamicCommand(
                commandBuilder.getName(),
                commandBuilder.getDescription(),
                commandBuilder.isNsfw(),
                commandBuilder.getCommandTypes(),
                commandBuilder.getCommandContextTypes(),
                commandBuilder.getRequiredPermissions(),
                commandBuilder.getArguments(),
                commandBuilder.getConsumer(),
                parent,
                group
        );

        if (parent == null) {
            requireUnusedName(command.name());
            register(command);
            dynamicCommands.add(command);
            return;
        }

        warnAboutIgnoredSettings(
                parent + (group == null ? "" : " " + group) + " " + command.name(), parent,
                command.nsfw(), command.commandTypes(), command.commandContextTypes(), command.requiredPermissions()
        );

        Set<String> changedParents = new LinkedHashSet<>();
        syncAfter(changedParents, () -> addSubcommand(command, changedParents));
    }

    @Override
    public void register(@NotNull CommandParentBuilder parentBuilder) {
        declareParent(RegisteredParent.of(parentBuilder));
    }

    /**
     * Discord only knows one command per name, a second handler would silently run as well.
     */
    private synchronized void requireUnusedName(@NotNull String name) {
        boolean taken = commands.stream().anyMatch(cmd -> cmd.parent() == null && cmd.name().equals(name))
                || dynamicCommands.stream().anyMatch(cmd -> cmd.parent() == null && cmd.name().equals(name))
                || parents.containsKey(name);
        if (taken)
            throw new IllegalArgumentException("A command named '" + name + "' is already registered");
    }

    /**
     * Declares a parent command, which can hold subcommands afterward.
     * Declaring a parent again keeps the first declaration, only additional groups get merged in.
     */
    private synchronized void declareParent(@NotNull RegisteredParent incoming) {
        // Let JDA validate names and descriptions, so a mistake shows up here instead of at the first subcommand
        Commands.slash(incoming.name(), incoming.description());
        incoming.groups().forEach(SubcommandGroupData::new);

        RegisteredParent existing = parents.get(incoming.name());
        if (existing == null) {
            boolean taken = commands.stream().anyMatch(cmd -> cmd.parent() == null && cmd.name().equals(incoming.name()))
                    || dynamicCommands.stream().anyMatch(cmd -> cmd.parent() == null && cmd.name().equals(incoming.name()));
            if (taken)
                throw new IllegalArgumentException("A command named '" + incoming.name() + "' is already registered, it can't also be a parent command");
        }

        Map<String, String> groups = new LinkedHashMap<>(existing == null ? Map.of() : existing.groups());
        for (Map.Entry<String, String> group : incoming.groups().entrySet()) {
            if (groups.containsKey(group.getKey())) {
                if (!groups.get(group.getKey()).equals(group.getValue()) && DebugLogging.isBasicEnabled())
                    debugLogger.debug("Group '{}' of command '{}' was already declared with another description, keeping the first", group.getKey(), incoming.name());
                continue;
            }

            if (subSpecs(incoming.name()).stream().anyMatch(sub -> sub.group() == null && sub.name().equals(group.getKey())))
                throw new IllegalArgumentException("Group '" + group.getKey() + "' of command '" + incoming.name() + "' clashes with a subcommand of the same name");

            groups.put(group.getKey(), group.getValue());
        }

        if (existing != null && existing.groups().size() == groups.size())
            return;

        parents.put(incoming.name(), (existing == null ? incoming : existing).withGroups(groups));
    }

    void registerMethod(@NotNull Method method, @NotNull CommandSupplier commandSupplier) {
        Set<String> changedParents = new LinkedHashSet<>();
        syncAfter(changedParents, () -> registerMethod(method, commandSupplier, changedParents));
    }

    /**
     * Registers a command method. Subcommands are only collected in changedParents,
     * as Discord wants every parent as a whole, which is sent by {@link #syncParents(Set)}
     */
    private void registerMethod(@NotNull Method method, @NotNull CommandSupplier commandSupplier, @NotNull Set<String> changedParents) {
        if (!method.trySetAccessible())
            throw new IllegalStateException(
                    "Cannot access command method: " + method.getDeclaringClass().getName()
                            + "#" + method.getName()
            );

        cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command commandInfo = method.getAnnotation(cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command.class);
        String parent = blankToNull(commandInfo.parent());
        String group = blankToNull(commandInfo.group());
        if (parent == null && group != null)
            throw new IllegalArgumentException("Command " + commandInfo.name() + " sets the group '" + group + "', but has no parent");

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

        RegisteredCommand command = new RegisteredCommand(
                commandInfo.name(),
                commandInfo.description(),
                commandInfo.nsfw(),
                commandInfo.commandType(),
                commandInfo.commandContextType(),
                commandInfo.requiredPermissions(),
                arguments,
                method,
                commandSupplier,
                parent,
                group
        );

        if (parent == null) {
            requireUnusedName(command.name());
            register(command);

            commands.add(
                    command
            );
            return;
        }

        warnAboutIgnoredSettings(
                parent + (group == null ? "" : " " + group) + " " + command.name(), parent,
                command.nsfw(), command.commandTypes(), command.commandContextTypes(), command.requiredPermissions()
        );
        addSubcommand(command, changedParents);
    }

    /**
     * Adds a subcommand after making sure it's valid, the parent isn't updated on Discord until {@link #syncParents(Set)}
     */
    private synchronized void addSubcommand(@NotNull RegisteredCommand command, @NotNull Set<String> changedParents) {
        String parent = Objects.requireNonNull(command.parent());
        requireSubcommandSlot(parent, command.group(), command.name(), command.description(), optionsOf(command));

        commands.add(command);
        changedParents.add(parent);
    }

    private synchronized void addSubcommand(@NotNull RegisteredDynamicCommand command, @NotNull Set<String> changedParents) {
        String parent = Objects.requireNonNull(command.parent());
        requireSubcommandSlot(parent, command.group(), command.name(), command.description(), optionsOf(command));

        dynamicCommands.add(command);
        changedParents.add(parent);
    }

    /**
     * Checks if a subcommand can be added, so a failure doesn't leave a half registered subcommand behind
     *
     * @throws IllegalStateException    The parent or the group wasn't declared
     * @throws IllegalArgumentException The subcommand exists already, or JDA/Discord rejects it (names, limits, ...)
     */
    private void requireSubcommandSlot(@NotNull String parent, @Nullable String group, @NotNull String name, @NotNull String description, @NotNull List<OptionData> options) {
        String path = parent + (group == null ? "" : " " + group) + " " + name;

        RegisteredParent registeredParent = parents.get(parent);
        if (registeredParent == null)
            throw new IllegalStateException("Subcommand '" + path + "' references the parent command '" + parent
                    + "', which wasn't declared. Declare it with @CommandParent (or a CommandParentBuilder) before registering its subcommands");

        if (group != null && !registeredParent.groups().containsKey(group))
            throw new IllegalStateException("Subcommand '" + path + "' references the group '" + group + "', which wasn't declared in the parent command '"
                    + parent + "'. Add it to the groups of @CommandParent (or use CommandParentBuilder#addGroup)");

        List<SubSpec> existing = subSpecs(parent);
        if (existing.stream().anyMatch(sub -> Objects.equals(sub.group(), group) && sub.name().equals(name)))
            throw new IllegalArgumentException("A subcommand '" + path + "' is already registered");

        if (group == null && registeredParent.groups().containsKey(name))
            throw new IllegalArgumentException("Subcommand '" + path + "' clashes with a group of the same name");

        if (DiscordAPI.nullableApi() == null) throw new RuntimeException("Command registration requested before ready");

        // JDA validates names, descriptions, options and the amount of subcommands whilst building
        List<SubSpec> all = new ArrayList<>(existing);
        all.add(new SubSpec(name, description, group, options));
        buildParentData(registeredParent, all);
    }

    private void warnAboutIgnoredSettings(@NotNull String path, @NotNull String parent, boolean nsfw, @NotNull CommandType[] commandTypes, @NotNull CommandContextType[] commandContextTypes, @NotNull Permission[] requiredPermissions) {
        List<String> ignored = new ArrayList<>();
        if (nsfw) ignored.add("nsfw");
        if (!Arrays.equals(commandTypes, DEFAULT_COMMAND_TYPES)) ignored.add("commandType");
        if (!Arrays.equals(commandContextTypes, DEFAULT_COMMAND_CONTEXT_TYPES)) ignored.add("commandContextType");
        if (requiredPermissions.length != 0) ignored.add("requiredPermissions");

        if (!ignored.isEmpty())
            logger.warn("Subcommand '{}' sets {}, which Discord only supports on top level commands. The settings of its parent command '{}' apply instead",
                    path, String.join(", ", ignored), parent);
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     * Sends the current state of the given parent commands to Discord, as Discord replaces a command as a whole
     */
    private void syncParents(@NotNull Set<String> parentNames) {
        for (String name : parentNames) {
            SlashCommandData data = currentParentData(name);
            if (data == null) continue;

            if (DiscordAPI.nullableApi() == null) throw new RuntimeException("Command registration requested before ready");
            DiscordAPI.nullableApi().upsertCommand(data).queue();
        }
    }

    /**
     * Runs the action, and sends the parents it changed to Discord afterward.
     * Subcommands which made it are sent even if the action failed later, as their handlers are registered as well.
     */
    private void syncAfter(@NotNull Set<String> changedParents, @NotNull Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            try {
                syncParents(changedParents);
            } catch (RuntimeException suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
        syncParents(changedParents);
    }

    /**
     * Drops all local command handlers, so commands of unloaded plugins aren't executed anymore.
     * Commands stay registered on Discord, as plugins re-register them after loading.
     */
    public void clearHandlers() {
        commands.clear();
        dynamicCommands.clear();
        parents.clear();
    }

    /**
     * Drops the command handlers of one plugin, identified by the class loader, which defined its handlers.
     * Use {@link #pruneUnusedCommands()} afterward, to remove them on Discord as well.
     */
    public synchronized void clearHandlers(@NotNull ClassLoader pluginLoader) {
        commands.removeIf(cmd -> cmd.commandSupplier().getClass().getClassLoader() == pluginLoader);
        dynamicCommands.removeIf(cmd -> cmd.consumer().getClass().getClassLoader() == pluginLoader);

        // A parent without subcommands is nothing without its plugin, and is declared again when that plugin loads
        parents.keySet().removeIf(name -> !hasSubcommands(name));
    }

    private boolean hasSubcommands(@NotNull String parent) {
        return commands.stream().anyMatch(cmd -> parent.equals(cmd.parent()))
                || dynamicCommands.stream().anyMatch(cmd -> parent.equals(cmd.parent()));
    }

    /**
     * Asynchronously deletes all commands on Discord, which have no registered handler.
     * Global commands are compared by name, guild commands are always unused, as only global commands get registered.
     * Guild commands are only removed with prune-guild-commands, as they aren't necessarily created by ComitasBotJ.
     * <p>
     * Parent commands are kept as long as they have a subcommand, but are updated if subcommands were removed.
     */
    public void pruneUnusedCommands() {
        var jda = DiscordAPI.nullableApi();
        if (jda == null)
            throw new IllegalStateException("Command pruning requested before ready");

        jda.retrieveCommands().queue(remoteCommands -> {
            // Resolve names on arrival, so commands registered in the meantime are kept
            Set<String> used = new HashSet<>();
            commands.forEach(cmd -> used.add(cmd.parent() == null ? cmd.name() : cmd.parent()));
            dynamicCommands.forEach(cmd -> used.add(cmd.parent() == null ? cmd.name() : cmd.parent()));

            for (Command command : remoteCommands) {
                if (used.contains(command.getName())) {
                    // Removed subcommands (e.g. of an unloaded plugin) would stay on Discord otherwise
                    SlashCommandData local = currentParentData(command.getName());
                    if (local != null && !subcommandPaths(command).equals(subcommandPaths(local))) {
                        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Updating subcommands of '{}'", command.getName());
                        jda.upsertCommand(local).queue(null, e -> logger.warn("Failed to update subcommands of '{}'", command.getName(), e));
                    }
                    continue;
                }
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Removing unused command '{}'", command.getName());
                command.delete().queue(null, e -> logger.warn("Failed to remove unused command '{}'", command.getName(), e));
            }
        }, e -> logger.warn("Failed to retrieve commands for pruning", e));

        if (!Main.conf().pruneGuildCommands.get()) return;

        for (Guild guild : jda.getGuilds())
            guild.retrieveCommands().queue(remoteCommands -> {
                for (Command command : remoteCommands) {
                    if (DebugLogging.isBasicEnabled())
                        debugLogger.debug("Removing unused guild command '{}' from {}", command.getName(), guild.getName());
                    command.delete().queue(null, e -> logger.warn("Failed to remove unused guild command '{}'", command.getName(), e));
                }
            }, e -> logger.warn("Failed to retrieve guild commands of {} for pruning", guild.getName(), e));
    }

    private static @NotNull Set<String> subcommandPaths(@NotNull Command command) {
        Set<String> paths = new HashSet<>();
        command.getSubcommands().forEach(sub -> paths.add(sub.getName()));
        command.getSubcommandGroups().forEach(group ->
                group.getSubcommands().forEach(sub -> paths.add(group.getName() + " " + sub.getName())));
        return paths;
    }

    private static @NotNull Set<String> subcommandPaths(@NotNull SlashCommandData data) {
        Set<String> paths = new HashSet<>();
        data.getSubcommands().forEach(sub -> paths.add(sub.getName()));
        data.getSubcommandGroups().forEach(group ->
                group.getSubcommands().forEach(sub -> paths.add(group.getName() + " " + sub.getName())));
        return paths;
    }

    /**
     * The slash command data of a parent with all of its current subcommands
     *
     * @return The data, or null if there is no such parent, or it has no subcommands (yet)
     */
    private @Nullable SlashCommandData currentParentData(@NotNull String parent) {
        RegisteredParent registeredParent = parents.get(parent);
        if (registeredParent == null) return null;

        List<SubSpec> specs = subSpecs(parent);
        if (specs.isEmpty()) return null;

        return buildParentData(registeredParent, specs);
    }

    private @NotNull List<SubSpec> subSpecs(@NotNull String parent) {
        List<SubSpec> specs = new ArrayList<>();
        for (RegisteredCommand cmd : commands)
            if (parent.equals(cmd.parent()))
                specs.add(new SubSpec(cmd.name(), cmd.description(), cmd.group(), optionsOf(cmd)));
        for (RegisteredDynamicCommand cmd : dynamicCommands)
            if (parent.equals(cmd.parent()))
                specs.add(new SubSpec(cmd.name(), cmd.description(), cmd.group(), optionsOf(cmd)));
        return specs;
    }

    private @NotNull SlashCommandData buildParentData(@NotNull RegisteredParent parent, @NotNull List<SubSpec> specs) {
        SlashCommandData data = baseData(
                parent.name(), parent.description(), parent.nsfw(),
                parent.commandTypes(), parent.commandContextTypes(), parent.requiredPermissions()
        );

        // Discord doesn't accept groups without subcommands, so only used groups are sent
        Map<String, SubcommandGroupData> groups = new LinkedHashMap<>();
        for (SubSpec spec : specs) {
            SubcommandData subcommand = new SubcommandData(spec.name(), spec.description()).addOptions(spec.options());

            if (spec.group() == null)
                data.addSubcommands(subcommand);
            else
                groups.computeIfAbsent(
                        spec.group(),
                        name -> new SubcommandGroupData(name, parent.groups().get(name))
                ).addSubcommands(subcommand);
        }
        data.addSubcommandGroups(groups.values());

        return data;
    }

    /**
     * The slash command data for everything every top level command and parent command has
     */
    private @NotNull SlashCommandData baseData(@NotNull String name, @NotNull String description, boolean nsfw, @NotNull CommandType[] commandTypes, @NotNull CommandContextType[] commandContextTypes, @NotNull Permission[] requiredPermissions) {
        SlashCommandData data = Commands.slash(name, description);

        data = data.setContexts(
                Arrays.stream(commandContextTypes)
                        .map(e -> InteractionContextType.fromKey(e.getKey()))
                        .toArray(InteractionContextType[]::new)
        );
        data = data.setIntegrationTypes(
                Arrays.stream(commandTypes)
                        .map(e -> IntegrationType.fromKey(e.getKey()))
                        .toArray(IntegrationType[]::new)
        );

        if (requiredPermissions.length == 0)
            data.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
        else
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(
                    net.dv8tion.jda.api.Permission.getPermissions(
                            Permission.asLong(requiredPermissions)
                    )
            ));

        return data.setNSFW(nsfw);
    }

    private @NotNull List<OptionData> optionsOf(@NotNull RegisteredCommand command) {
        List<OptionData> options = new ArrayList<>();
        for (CommandArgument<?> arg : command.arguments())
            if (arg.type() != CommandArgumentType.CONTEXT) {
                if (arg.type().optionType() == null || arg.name() == null || arg.description() == null)
                    throw new IllegalArgumentException("Command argument " + arg.type() + " isn't a supported type!");
                options.add(new OptionData(
                        arg.type().optionType(), arg.name(), arg.description(), arg.required())
                );
            }
        return options;
    }

    private @NotNull List<OptionData> optionsOf(@NotNull RegisteredDynamicCommand command) {
        List<OptionData> options = new ArrayList<>();
        for (DynamicCommandBuilder.ICommandArgument arg : command.arguments())
            options.add(new OptionData(
                    optionTypeFromCommandArgumentType(arg.argumentType()),
                    arg.identifier(),
                    arg.description(),
                    arg.required()
            ));
        return options;
    }

    void register(@NotNull RegisteredCommand command) {
        if (DiscordAPI.nullableApi() == null) throw new RuntimeException("Command registration requested before ready");
        SlashCommandData data = baseData(
                command.name(), command.description(), command.nsfw(),
                command.commandTypes(), command.commandContextTypes(), command.requiredPermissions()
        );

        data = data.addOptions(optionsOf(command));

        DiscordAPI.nullableApi().upsertCommand(data).queue();
    }

    void register(@NotNull RegisteredDynamicCommand command) {
        if (DiscordAPI.nullableApi() == null) throw new RuntimeException("Command registration requested before ready");
        SlashCommandData data = baseData(
                command.name(), command.description(), command.nsfw(),
                command.commandTypes(), command.commandContextTypes(), command.requiredPermissions()
        );

        data = data.addOptions(optionsOf(command));

        DiscordAPI.nullableApi().upsertCommand(data).queue();
    }

    @NotNull
    private OptionType optionTypeFromCommandArgumentType(@NotNull cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType<?> type) {
        return switch (type.name()) {
            case "STRING" -> OptionType.STRING;
            case "NUMBER" -> OptionType.INTEGER;
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
            case INTEGER -> cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.NUMBER;
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
            case INTEGER -> argumentType.cast(mapping.getAsLong());
            case NUMBER -> argumentType.cast(mapping.getAsDouble());
            case BOOLEAN -> argumentType.cast(mapping.getAsBoolean());
            case USER -> argumentType.cast(new InternalUser(mapping.getAsUser()));
            case CHANNEL -> argumentType.cast(ChannelTypeResolver.resolve(mapping.getAsChannel()));
            case ROLE -> argumentType.cast(new InternalRole(mapping.getAsRole()));
            case ATTACHMENT -> argumentType.cast(new InternalAttachment(mapping.getAsAttachment()));
            default -> throw new IllegalStateException("Unexpected value: " + mapping.getType());
        };
    }

    record RegisteredCommand(String name, String description, boolean nsfw, CommandType[] commandTypes, CommandContextType[] commandContextTypes, Permission[] requiredPermissions, CommandArgument<?>[] arguments, Method method, CommandSupplier commandSupplier, @Nullable String parent, @Nullable String group) {}

    record RegisteredDynamicCommand(String name, String description, boolean nsfw, CommandType[] commandTypes,
                                    CommandContextType[] commandContextTypes,
                                    Permission[] requiredPermissions,
                                    List<DynamicCommandBuilder.ICommandArgument> arguments,
                                    BiConsumer<CommandRanContext, CommandArgumentProvider> consumer,
                                    @Nullable String parent, @Nullable String group) {
    }

    /**
     * A declared parent command, with its subcommand groups (name to description)
     */
    record RegisteredParent(@NotNull String name, @NotNull String description, boolean nsfw,
                            @NotNull CommandType[] commandTypes,
                            @NotNull CommandContextType[] commandContextTypes,
                            @NotNull Permission[] requiredPermissions,
                            @NotNull Map<String, String> groups) {
        static @NotNull RegisteredParent of(@NotNull CommandParent parent) {
            Map<String, String> groups = new LinkedHashMap<>();
            for (CommandGroup group : parent.groups())
                if (groups.putIfAbsent(group.name(), group.description()) != null)
                    throw new IllegalArgumentException("Command " + parent.name() + " declares the group '" + group.name() + "' multiple times");

            return new RegisteredParent(parent.name(), parent.description(), parent.nsfw(), parent.commandType(),
                    parent.commandContextType(), parent.requiredPermissions(), groups);
        }

        static @NotNull RegisteredParent of(@NotNull CommandParentBuilder builder) {
            Map<String, String> groups = new LinkedHashMap<>();
            for (CommandParentBuilder.Group group : builder.getGroups())
                if (groups.putIfAbsent(group.name(), group.description()) != null)
                    throw new IllegalArgumentException("Command " + builder.getName() + " declares the group '" + group.name() + "' multiple times");

            return new RegisteredParent(builder.getName(), builder.getDescription(), builder.isNsfw(), builder.getCommandTypes(),
                    builder.getCommandContextTypes(), builder.getRequiredPermissions(), groups);
        }

        @NotNull RegisteredParent withGroups(@NotNull Map<String, String> groups) {
            return new RegisteredParent(name, description, nsfw, commandTypes, commandContextTypes, requiredPermissions, Collections.unmodifiableMap(new LinkedHashMap<>(groups)));
        }
    }

    /**
     * What a subcommand looks like on Discord
     */
    record SubSpec(@NotNull String name, @NotNull String description, @Nullable String group, @NotNull List<OptionData> options) {
    }
}
