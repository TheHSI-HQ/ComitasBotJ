package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandContextType;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandGroup;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandOption;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandParent;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRanContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandSupplier;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandParentBuilder;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.DynamicCommandBuilder;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.Discord.Commands.InternalCommandRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Registration and dispatch of subcommands, with JDA and the interaction faked, so nothing talks to Discord
 */
class CommandRegistrySubcommandTest {
    /**
     * What the command methods below did, as "command: details"
     */
    private static final @NotNull List<String> CALLS = new ArrayList<>();

    private final @NotNull List<SlashCommandData> upserts = new ArrayList<>();
    private final @NotNull List<String> deleted = new ArrayList<>();
    private @NotNull List<net.dv8tion.jda.api.interactions.commands.Command> remote = List.of();
    private @NotNull InternalCommandRegistry registry = new InternalCommandRegistry();

    @BeforeEach
    void setUp() {
        CALLS.clear();
        DiscordAPI.api = fakeJda();
        registry = new InternalCommandRegistry();
    }

    @AfterEach
    void tearDown() {
        DiscordAPI.api = null;
    }

    // ---------------------------------------------------------------- command suppliers

    @CommandParent(
            name = "mod", description = "Moderation",
            groups = @CommandGroup(name = "roles", description = "Manage roles")
    )
    public static class ModCommands implements CommandSupplier {
        @Command(parent = "mod", name = "ban", description = "Ban someone")
        public void ban(
                @NotNull CommandRanContext ctx,
                @CommandOption(name = "reason", description = "Why", required = false) @Nullable String reason
        ) {
            CALLS.add("ban: " + ctx.getCommandName() + "/" + ctx.getSubcommandGroup() + "/" + ctx.getSubcommandName());
        }

        @Command(parent = "mod", name = "kick", description = "Kick someone")
        public void kick(@NotNull CommandRanContext ctx) {
            CALLS.add("kick");
        }

        @Command(parent = "mod", group = "roles", name = "add", description = "Add a role")
        public void addRole(@NotNull CommandRanContext ctx) {
            CALLS.add("roles add: " + ctx.getCommandName() + "/" + ctx.getSubcommandGroup() + "/" + ctx.getSubcommandName());
        }

        @Command(parent = "mod", group = "roles", name = "remove", description = "Remove a role")
        public void removeRole(@NotNull CommandRanContext ctx) {
            CALLS.add("roles remove");
        }

        @Command(name = "ping", description = "A regular command")
        public void ping(@NotNull CommandRanContext ctx) {
            CALLS.add("ping");
        }
    }

    @CommandParent(name = "admin", description = "Administration", nsfw = true,
            commandContextType = {CommandContextType.GUILD, CommandContextType.BOT_DM},
            requiredPermissions = {Permission.ADMINISTRATOR})
    public static class AdminParentOnly implements CommandSupplier {
    }

    public static class AdminSubcommands implements CommandSupplier {
        // The permissions are ignored (with a warning), the parent decides
        @Command(parent = "admin", name = "restart", description = "Restart",
                requiredPermissions = {Permission.KICK_MEMBERS})
        public void restart(@NotNull CommandRanContext ctx) {
            CALLS.add("restart");
        }
    }

    public static class OrphanSubcommand implements CommandSupplier {
        @Command(parent = "nothing", name = "sub", description = "No parent declared")
        public void sub(@NotNull CommandRanContext ctx) {
        }
    }

    @CommandParent(name = "tools", description = "Tools")
    public static class UndeclaredGroup implements CommandSupplier {
        @Command(parent = "tools", group = "missing", name = "sub", description = "No group declared")
        public void sub(@NotNull CommandRanContext ctx) {
        }
    }

    public static class GroupWithoutParent implements CommandSupplier {
        @Command(group = "lonely", name = "sub", description = "Group without parent")
        public void sub(@NotNull CommandRanContext ctx) {
        }
    }

    public static class RegularCommand implements CommandSupplier {
        @Command(name = "secret", description = "A regular command with settings", nsfw = true,
                commandType = {cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandType.USER_INSTALL},
                commandContextType = {CommandContextType.BOT_DM},
                requiredPermissions = {Permission.ADMINISTRATOR})
        public void secret(
                @NotNull CommandRanContext ctx,
                @CommandOption(name = "target", description = "Who") @NotNull cloud.thehsi.ComitasBotJ.API.Discord.User.User target,
                @CommandOption(name = "note", description = "Why", required = false) @Nullable String note
        ) {
        }
    }

    @CommandParent(name = "mod", description = "Moderation")
    public static class RegularCommandNamedLikeAParent implements CommandSupplier {
        @Command(name = "mod", description = "Clashes with the mod parent")
        public void mod(@NotNull CommandRanContext ctx) {
        }
    }

    @CommandParent(name = "Upper", description = "Uppercase names aren't allowed")
    public static class UppercaseParent implements CommandSupplier {
    }

    // ---------------------------------------------------------------- registration

    @Test
    void subcommandsOfAParentAreSentAsOneCommand() {
        registry.register(new ModCommands());

        // /ping, and a single /mod containing all four subcommands
        List<String> names = upserts.stream().map(SlashCommandData::getName).sorted().toList();
        assertEquals(List.of("mod", "ping"), names);

        SlashCommandData mod = upsert("mod");
        assertEquals("Moderation", mod.getDescription());
        assertEquals(List.of("ban", "kick"), mod.getSubcommands().stream().map(SubcommandData::getName).sorted().toList());
        assertEquals(1, mod.getSubcommandGroups().size());

        SubcommandGroupData roles = mod.getSubcommandGroups().getFirst();
        assertEquals("roles", roles.getName());
        assertEquals("Manage roles", roles.getDescription());
        assertEquals(List.of("add", "remove"), roles.getSubcommands().stream().map(SubcommandData::getName).sorted().toList());

        // Options end up on the subcommand, not on the parent
        assertTrue(mod.getOptions().isEmpty());
        SubcommandData ban = mod.getSubcommands().stream().filter(sub -> sub.getName().equals("ban")).findFirst().orElseThrow();
        assertEquals(1, ban.getOptions().size());
        assertEquals("reason", ban.getOptions().getFirst().getName());
        assertEquals(OptionType.STRING, ban.getOptions().getFirst().getType());
        assertFalse(ban.getOptions().getFirst().isRequired());
    }

    @Test
    void parentSettingsApplyToTheWholeCommand() {
        registry.register(new AdminParentOnly());
        assertTrue(upserts.isEmpty(), "A parent without subcommands must not be sent to Discord");

        registry.register(new AdminSubcommands());

        SlashCommandData admin = upsert("admin");
        assertTrue(admin.isNSFW());
        assertEquals(2, admin.getContexts().size());
        assertEquals(
                net.dv8tion.jda.api.Permission.ADMINISTRATOR.getRawValue(),
                admin.getDefaultPermissions().getPermissionsRaw()
        );
        assertEquals(List.of("restart"), admin.getSubcommands().stream().map(SubcommandData::getName).toList());
    }

    @Test
    void regularCommandsAreSentExactlyAsBefore() {
        registry.register(new RegularCommand());

        assertEquals(1, upserts.size());
        SlashCommandData secret = upserts.getFirst();
        assertEquals("secret", secret.getName());
        assertEquals("A regular command with settings", secret.getDescription());
        assertTrue(secret.isNSFW());
        assertTrue(secret.getSubcommands().isEmpty());
        assertTrue(secret.getSubcommandGroups().isEmpty());
        assertEquals(java.util.Set.of(net.dv8tion.jda.api.interactions.InteractionContextType.BOT_DM), secret.getContexts());
        assertEquals(java.util.Set.of(net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL), secret.getIntegrationTypes());
        assertEquals(net.dv8tion.jda.api.Permission.ADMINISTRATOR.getRawValue(), secret.getDefaultPermissions().getPermissionsRaw());

        assertEquals(List.of("target", "note"), secret.getOptions().stream().map(net.dv8tion.jda.api.interactions.commands.build.OptionData::getName).toList());
        assertEquals(OptionType.USER, secret.getOptions().get(0).getType());
        assertTrue(secret.getOptions().get(0).isRequired());
        assertEquals(OptionType.STRING, secret.getOptions().get(1).getType());
        assertFalse(secret.getOptions().get(1).isRequired());
    }

    @Test
    void parentCanBeDeclaredInAnotherSupplierBeforeItsSubcommands() {
        registry.register(new AdminParentOnly());
        registry.register(new AdminSubcommands());

        assertEquals(1, upserts.size());
    }

    @Test
    void subcommandWithoutDeclaredParentIsRejected() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> registry.register(new OrphanSubcommand()));

        assertTrue(e.getMessage().contains("nothing sub"), e.getMessage());
        assertTrue(e.getMessage().contains("@CommandParent"), e.getMessage());
        assertTrue(upserts.isEmpty());
    }

    @Test
    void subcommandWithUndeclaredGroupIsRejected() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> registry.register(new UndeclaredGroup()));

        assertTrue(e.getMessage().contains("missing"), e.getMessage());
        assertTrue(upserts.isEmpty());
    }

    @Test
    void groupWithoutParentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(new GroupWithoutParent()));
    }

    @Test
    void registeringTheSameSubcommandsTwiceIsRejected() {
        registry.register(new AdminParentOnly());
        registry.register(new AdminSubcommands());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> registry.register(new AdminSubcommands()));
        assertTrue(e.getMessage().contains("already registered"), e.getMessage());

        // The duplicate wasn't added to the handlers, restart still runs once
        registry.handleCommand(event("admin", null, "restart"));
        assertEquals(List.of("restart"), CALLS);
    }

    @Test
    void regularCommandsStillCanNotBeRegisteredTwice() {
        registry.register(new ModCommands());

        assertThrows(IllegalArgumentException.class, () -> registry.register(
                new DynamicCommandBuilder("ping", "Taken by the regular ping", (ctx, args) -> {
                })
        ));
    }

    @Test
    void parentCanNotBeDeclaredOverARegularCommand() {
        registry.register(new DynamicCommandBuilder("mod", "A regular command first", (ctx, args) -> {
        }));

        assertThrows(IllegalArgumentException.class, () -> registry.register(new RegularCommandNamedLikeAParent()));
    }

    @Test
    void regularCommandCanNotUseTheNameOfAParent() {
        registry.register(new CommandParentBuilder("mod", "Moderation"));

        assertThrows(IllegalArgumentException.class, () -> registry.register(
                new DynamicCommandBuilder("mod", "Clash", (ctx, args) -> {
                })
        ));
    }

    @Test
    void invalidNamesAreRejectedBeforeAnythingIsSent() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(new UppercaseParent()));

        registry.register(new CommandParentBuilder("tools", "Tools"));
        assertThrows(IllegalArgumentException.class, () -> registry.register(
                new DynamicCommandBuilder("Sub", "Uppercase", (ctx, args) -> {
                }).setParent("tools")
        ));
        assertTrue(upserts.isEmpty());
    }

    @Test
    void discordsLimitOf25SubcommandsIsEnforcedWithoutLeavingAHandlerBehind() {
        registry.register(new CommandParentBuilder("many", "Many subcommands"));

        for (int i = 0; i < 25; i++) {
            int number = i;
            registry.register(new DynamicCommandBuilder("sub" + i, "Subcommand", (ctx, args) -> CALLS.add("sub" + number)).setParent("many"));
        }
        assertThrows(IllegalArgumentException.class, () -> registry.register(
                new DynamicCommandBuilder("sub25", "One too many", (ctx, args) -> CALLS.add("sub25")).setParent("many")
        ));

        registry.handleCommand(event("many", null, "sub25"));
        assertTrue(CALLS.isEmpty(), "The rejected subcommand must not have a handler");
        registry.handleCommand(event("many", null, "sub24"));
        assertEquals(List.of("sub24"), CALLS);
    }

    @Test
    void additionalGroupsOfARepeatedParentAreMergedAndTheFirstDescriptionWins() {
        registry.register(new CommandParentBuilder("cfg", "First").addGroup("a", "Group A"));
        registry.register(new CommandParentBuilder("cfg", "Second").addGroup("a", "Other").addGroup("b", "Group B"));
        registry.register(new DynamicCommandBuilder("one", "In a", (ctx, args) -> {
        }).setParent("cfg").setGroup("a"));
        registry.register(new DynamicCommandBuilder("two", "In b", (ctx, args) -> {
        }).setParent("cfg").setGroup("b"));

        SlashCommandData cfg = upserts.getLast();
        assertEquals("First", cfg.getDescription());
        Map<String, String> groups = cfg.getSubcommandGroups().stream()
                .collect(Collectors.toMap(SubcommandGroupData::getName, SubcommandGroupData::getDescription));
        assertEquals(Map.of("a", "Group A", "b", "Group B"), groups);
    }

    @Test
    void groupsWithoutSubcommandsAreNotSent() {
        registry.register(new CommandParentBuilder("cfg", "Config").addGroup("empty", "Nothing in here").addGroup("used", "Used"));
        registry.register(new DynamicCommandBuilder("one", "In used", (ctx, args) -> {
        }).setParent("cfg").setGroup("used"));

        SlashCommandData cfg = upsert("cfg");
        assertEquals(List.of("used"), cfg.getSubcommandGroups().stream().map(SubcommandGroupData::getName).toList());
    }

    // ---------------------------------------------------------------- dispatch

    @Test
    void eachSubcommandReachesItsOwnMethod() {
        registry.register(new ModCommands());

        registry.handleCommand(event("mod", null, "ban"));
        registry.handleCommand(event("mod", null, "kick"));
        registry.handleCommand(event("mod", "roles", "add"));
        registry.handleCommand(event("mod", "roles", "remove"));

        assertEquals(List.of("ban: mod/null/ban", "kick", "roles add: mod/roles/add", "roles remove"), CALLS);
    }

    @Test
    void regularCommandsStillWorkNextToParents() {
        registry.register(new ModCommands());

        registry.handleCommand(event("ping", null, null));

        assertEquals(List.of("ping"), CALLS);
    }

    @Test
    void subcommandsAreOnlyMatchedWithTheirParentAndGroup() {
        registry.register(new ModCommands());

        registry.handleCommand(event("mod", null, null));          // the parent itself
        registry.handleCommand(event("other", null, "ban"));       // another parent
        registry.handleCommand(event("mod", "roles", "ban"));      // right name, wrong group
        registry.handleCommand(event("mod", null, "add"));         // right name, missing group
        registry.handleCommand(event("ping", null, "ban"));        // regular command with a subcommand

        assertTrue(CALLS.isEmpty(), CALLS.toString());
    }

    @Test
    void dynamicSubcommandsAreRegisteredAndDispatched() {
        registry.register(new CommandParentBuilder("dyn", "Dynamic").addGroup("g", "Group"));
        assertTrue(upserts.isEmpty());

        registry.register(new DynamicCommandBuilder("direct", "Direct", (ctx, args) ->
                CALLS.add("direct: " + ctx.getCommandName() + "/" + ctx.getSubcommandGroup() + "/" + ctx.getSubcommandName())
        ).setParent("dyn").addArgument("text", "Some text", cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType.STRING));
        registry.register(new DynamicCommandBuilder("grouped", "Grouped", (ctx, args) ->
                CALLS.add("grouped: " + ctx.getSubcommandGroup())
        ).setParent("dyn").setGroup("g"));

        // Every dynamic subcommand is sent on its own, the last one has everything
        assertEquals(2, upserts.size());
        SlashCommandData dyn = upserts.getLast();
        assertEquals("dyn", dyn.getName());
        assertEquals(List.of("direct"), dyn.getSubcommands().stream().map(SubcommandData::getName).toList());
        assertEquals("text", dyn.getSubcommands().getFirst().getOptions().getFirst().getName());
        assertEquals(List.of("grouped"), dyn.getSubcommandGroups().getFirst().getSubcommands().stream().map(SubcommandData::getName).toList());

        registry.handleCommand(event("dyn", null, "direct"));
        registry.handleCommand(event("dyn", "g", "grouped"));
        assertEquals(List.of("direct: dyn/null/direct", "grouped: g"), CALLS);
    }

    // ---------------------------------------------------------------- unloading

    @Test
    void clearingHandlersDropsSubcommandsAndParentsWithoutSubcommands() {
        registry.register(new ModCommands());

        registry.clearHandlers(CommandRegistrySubcommandTest.class.getClassLoader());

        registry.handleCommand(event("mod", null, "kick"));
        assertTrue(CALLS.isEmpty());

        // The parent is gone as well, so its name is free again and subcommands need a new declaration
        assertDoesNotThrow(() -> registry.register(new DynamicCommandBuilder("mod", "Now regular", (ctx, args) -> {
        })));
        registry.clearHandlers();
        assertThrows(IllegalStateException.class, () -> registry.register(new OrphanSubcommand()));
    }

    @Test
    void clearingAllHandlersDropsParents() {
        registry.register(new ModCommands());
        registry.clearHandlers();

        assertDoesNotThrow(() -> registry.register(new DynamicCommandBuilder("mod", "Now regular", (ctx, args) -> {
        })));
    }

    // ---------------------------------------------------------------- pruning

    @Test
    void pruningKeepsParentsAndRemovesUnknownCommands() {
        registry.register(new ModCommands());
        upserts.clear();

        remote = List.of(
                remoteCommand("mod", List.of("ban", "kick"), Map.of("roles", List.of("add", "remove"))),
                remoteCommand("ping", List.of(), Map.of()),
                remoteCommand("stale", List.of(), Map.of())
        );
        prune();

        assertEquals(List.of("stale"), deleted);
        assertTrue(upserts.isEmpty(), "Nothing changed for mod, it must not be sent again");
    }

    @Test
    void pruningUpdatesAParentWhoseSubcommandsWereRemoved() {
        registry.register(new ModCommands());
        upserts.clear();

        // Discord still knows "extra", from a plugin which was unloaded since
        remote = List.of(
                remoteCommand("mod", List.of("ban", "kick", "extra"), Map.of("roles", List.of("add", "remove"))),
                remoteCommand("ping", List.of(), Map.of())
        );
        prune();

        assertTrue(deleted.isEmpty());
        assertEquals(1, upserts.size());
        assertEquals("mod", upserts.getFirst().getName());
        assertEquals(List.of("ban", "kick"), upserts.getFirst().getSubcommands().stream().map(SubcommandData::getName).sorted().toList());
    }

    @Test
    void pruningRemovesAParentOnceItHasNoSubcommandsLeft() {
        registry.register(new ModCommands());
        registry.clearHandlers(CommandRegistrySubcommandTest.class.getClassLoader());
        upserts.clear();

        remote = List.of(remoteCommand("mod", List.of("ban", "kick"), Map.of("roles", List.of("add", "remove"))));
        prune();

        assertEquals(List.of("mod"), deleted);
    }

    // ---------------------------------------------------------------- fakes

    private void prune() {
        try {
            registry.pruneUnusedCommands();
        } catch (IllegalStateException e) {
            // Command handling is done by now, only the guild part needs the server config, which isn't loaded in tests
            assertEquals("ServerConfig not initialized", e.getMessage());
        }
    }

    private @NotNull SlashCommandData upsert(@NotNull String name) {
        return upserts.stream().filter(data -> data.getName().equals(name)).reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("Command " + name + " wasn't sent to Discord"));
    }

    private @NotNull SlashCommandInteractionEvent event(@NotNull String name, @Nullable String group, @Nullable String subcommand) {
        net.dv8tion.jda.api.entities.User user = proxy(net.dv8tion.jda.api.entities.User.class, Map.of());
        MessageChannelUnion channel = proxy(
                MessageChannelUnion.class,
                Map.of("getType", args -> net.dv8tion.jda.api.entities.channel.ChannelType.TEXT)
        );

        Map<String, Handler> handlers = new java.util.HashMap<>();
        handlers.put("getName", args -> name);
        handlers.put("getSubcommandGroup", args -> group);
        handlers.put("getSubcommandName", args -> subcommand);
        handlers.put("getOptions", args -> List.of());
        handlers.put("getUser", args -> user);
        handlers.put("getChannel", args -> channel);

        return new SlashCommandInteractionEvent(DiscordAPI.api(), 1L, proxy(SlashCommandInteraction.class, handlers));
    }

    private @NotNull net.dv8tion.jda.api.interactions.commands.Command remoteCommand(@NotNull String name, @NotNull List<String> subcommands, @NotNull Map<String, List<String>> groups) {
        Map<String, Handler> handlers = new java.util.HashMap<>();
        net.dv8tion.jda.api.interactions.commands.Command command = proxy(net.dv8tion.jda.api.interactions.commands.Command.class, handlers);

        handlers.put("getName", args -> name);
        handlers.put("getSubcommands", args -> subcommands.stream().map(sub -> subcommand(command, sub)).toList());
        handlers.put("getSubcommandGroups", args -> groups.entrySet().stream().map(group -> new net.dv8tion.jda.api.interactions.commands.Command.SubcommandGroup(
                command,
                DataObject.empty()
                        .put("name", group.getKey()).put("description", "d")
                        .put("options", DataArray.fromCollection(group.getValue().stream()
                                .map(sub -> DataObject.empty().put("type", 1).put("name", sub).put("description", "d").toMap())
                                .toList()))
        )).toList());
        handlers.put("delete", args -> restAction(null, () -> deleted.add(name)));
        return command;
    }

    private static net.dv8tion.jda.api.interactions.commands.Command.Subcommand subcommand(@NotNull net.dv8tion.jda.api.interactions.commands.Command command, @NotNull String name) {
        return new net.dv8tion.jda.api.interactions.commands.Command.Subcommand(
                command, DataObject.empty().put("name", name).put("description", "d")
        );
    }

    private @NotNull JDA fakeJda() {
        Map<String, Handler> handlers = new java.util.HashMap<>();
        handlers.put("upsertCommand", args -> {
            upserts.add((SlashCommandData) args[0]);
            return restAction(null, () -> {
            });
        });
        handlers.put("retrieveCommands", args -> restAction(remote, () -> {
        }));
        handlers.put("getGuilds", args -> List.of());
        return proxy(JDA.class, handlers);
    }

    /**
     * A RestAction, which succeeds immediately with the result, instead of asking Discord
     */
    @SuppressWarnings("unchecked")
    private static @NotNull Object restAction(@Nullable Object result, @NotNull Runnable onQueue) {
        return Proxy.newProxyInstance(
                CommandRegistrySubcommandTest.class.getClassLoader(),
                new Class<?>[]{RestAction.class, net.dv8tion.jda.api.requests.restaction.CommandCreateAction.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("queue")) {
                        onQueue.run();
                        if (args != null && args.length > 0 && args[0] instanceof Consumer<?> success)
                            ((Consumer<Object>) success).accept(result);
                        return null;
                    }
                    return objectMethod(proxy, method, args);
                }
        );
    }

    private interface Handler {
        @Nullable Object handle(@Nullable Object[] args);
    }

    @SuppressWarnings("unchecked")
    private static <T> @NotNull T proxy(@NotNull Class<T> type, @NotNull Map<String, Handler> handlers) {
        InvocationHandler handler = (proxy, method, args) -> {
            Handler found = handlers.get(method.getName());
            if (found != null) return found.handle(args);
            return objectMethod(proxy, method, args);
        };
        return (T) Proxy.newProxyInstance(CommandRegistrySubcommandTest.class.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static @Nullable Object objectMethod(@NotNull Object proxy, @NotNull Method method, @Nullable Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "Fake" + proxy.getClass().getSimpleName();
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> defaultValue(method.getReturnType());
        };
    }

    private static @Nullable Object defaultValue(@NotNull Class<?> type) {
        if (type == boolean.class) return false;
        if (type == long.class) return 0L;
        if (type == int.class) return 0;
        if (List.class.isAssignableFrom(type)) return List.of();
        return null;
    }
}
