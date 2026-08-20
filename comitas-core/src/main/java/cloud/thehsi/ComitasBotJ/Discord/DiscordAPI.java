package cloud.thehsi.ComitasBotJ.Discord;

import ch.qos.logback.classic.Level;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageReceivedEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleAddedEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleRemovedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Commands.InternalCommandRegistry;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.Reaction.InternalReaction;
import cloud.thehsi.ComitasBotJ.Discord.Reaction.InternalReactionAction;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Event.Events.*;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DiscordAPI extends ListenerAdapter {
    static JDA api;
    final EventManager eventManager;
    final InternalCommandRegistry commandRegistry;
    private static final Logger debugLogger = DebugLogging.getLogger();
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".DiscordAPI");
    final List<RoleModificationLoopFix> roleModificationLoopFixList = Collections.synchronizedList(new ArrayList<>());
    private final String BOT_TOKEN;
    boolean firstStartup = true;

    public DiscordAPI(String BOT_TOKEN, EventManager eventManager, InternalCommandRegistry commandRegistry) {
        this.BOT_TOKEN = BOT_TOKEN;

        this.eventManager = eventManager;
        this.commandRegistry = commandRegistry;

        connect();
    }

    private DiscordAPI() {
        this.eventManager = null;
        this.commandRegistry = null;
        this.BOT_TOKEN = null;
    }

    public static JDA api() {
        return api;
    }

    public static DiscordAPI performMinimalStartup(String BOT_TOKEN, boolean silent) {
        ch.qos.logback.classic.Logger jdaLogger =
                (ch.qos.logback.classic.Logger)
                        LoggerFactory.getLogger("net.dv8tion.jda");

        if (DebugLogging.isAPIEnabled())
            jdaLogger.setLevel(
                    Level.DEBUG
            );
        else if (silent)
            jdaLogger.setLevel(
                    Level.WARN
            );
        else
            jdaLogger.setLevel(
                    Level.INFO
            );

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Connecting to Discord Bot in minimal mode");

        JDA api = JDABuilder.createLight(BOT_TOKEN).build();

        try {
            api.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        DiscordAPI.api = api;
        return new DiscordAPI();
    }

    private void connect() {
        ch.qos.logback.classic.Logger jdaLogger =
                (ch.qos.logback.classic.Logger)
                        LoggerFactory.getLogger("net.dv8tion.jda");

        jdaLogger.setLevel(
                DebugLogging.isAPIEnabled() ? Level.DEBUG : Level.INFO
        );

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Connecting to Discord Bot");
        api = JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(this)
                .build();
    }

    public void reconnect() throws InterruptedException {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Reconnecting to Discord Bot");
        if (api != null) {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Shutting bot down.");
            api.shutdown();

            if (!api.awaitShutdown(15, TimeUnit.SECONDS)) {
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Bot shutdown exceeded timeout, forcing shutdown.");
                api.shutdownNow();
            }
        }

        connect();
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Awaiting bot to be ready.");
        api.awaitReady();
    }

    public JDA getAPI() {
        return api;
    }

    @Override
    public void onMessageReceived(@NotNull net.dv8tion.jda.api.events.message.MessageReceivedEvent event) {
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a MessageReceivedEvent.");
        MessageReceivedEvent messageReceivedEvent = new InternalMessageReceivedEvent(event);

        eventManager.callEvent(messageReceivedEvent);

        if (messageReceivedEvent.markedForDeletion()) event.getMessage().delete().queue(ignored -> {
        }, error -> {
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Command {} got ran, passingto registry.", event.getName());
        commandRegistry.handleCommand(event);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Setting Bot Presence.");

        if (!Main.conf().botActivityName.get().isBlank())
            api.getPresence().setActivity(
                    Activity.watching(Main.conf().botActivityName.get())
            );

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Updating Command Registry Discord API reference.");
        commandRegistry.setDiscordApi(this);

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a BotReadyEvent.");
        eventManager.callEvent(new InternalBotReadyEvent(
                api.getSelfUser()
        ));

        if (firstStartup) {
            if (Main.props().noCmd() || Main.props().strictSafeMode())
                logger.info("Done ({}s)! Press CTRL+C (^C) to quit", Main.getRuntimeMS() / 1000d);
            else
                logger.info("Done ({}s)! For help, type \"help\"", Main.getRuntimeMS() / 1000d);
        }

        firstStartup = false;
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        for (Role role : event.getRoles()) {
            if (roleModificationLoopFixList.contains(
                    new RoleModificationLoopFix(true, event.getUser().getIdLong(), role.getIdLong()
                    ))) {
                if (DebugLogging.isEventEnabled()) //noinspection LoggingSimilarMessage
                    debugLogger.debug("Ignoring event for role {}, prevented by RoleModificationLoopFix", role.getName());
                roleModificationLoopFixList.remove(new RoleModificationLoopFix(true, event.getUser().getIdLong(), role.getIdLong()));
                continue;
            }

            UserRoleAddedEvent userRoleAddedEvent = new InternalUserRoleAddedEvent(
                    new InternalMember(event.getMember()),
                    new InternalRole(role)
            );

            if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserRoleAddedEvent.");
            eventManager.callEvent(userRoleAddedEvent);

            if (userRoleAddedEvent.willUndo()) {
                if (DebugLogging.isEventEnabled()) debugLogger.debug("Adding a RoleModificationLopeFix, as role addition was marked to be undone");
                roleModificationLoopFixList.add(
                        new RoleModificationLoopFix(false, event.getUser().getIdLong(), role.getIdLong())
                );
                event.getGuild().removeRoleFromMember(event.getUser(), role).queue(ignored -> {
                }, error -> {
                });
            }
        }
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        for (Role role : event.getRoles()) {
            if (roleModificationLoopFixList.contains(
                    new RoleModificationLoopFix(false, event.getUser().getIdLong(), role.getIdLong()
                    ))) {
                if (DebugLogging.isEventEnabled()) //noinspection LoggingSimilarMessage
                    debugLogger.debug("Ignoring event for role {}, prevented by RoleModificationLoopFix", role.getName());
                roleModificationLoopFixList.remove(new RoleModificationLoopFix(false, event.getUser().getIdLong(), role.getIdLong()));
                continue;
            }

            UserRoleRemovedEvent userRoleRemovedEvent = new InternalUserRoleRemovedEvent(
                    new InternalMember(event.getMember()),
                    new InternalRole(role)
            );

            if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserRoleRemovedEvent.");
            eventManager.callEvent(userRoleRemovedEvent);

            if (userRoleRemovedEvent.willUndo()) {
                if (DebugLogging.isEventEnabled()) debugLogger.debug("Adding a RoleModificationLopeFix, as role removal was marked to be undone");
                roleModificationLoopFixList.add(
                        new RoleModificationLoopFix(true, event.getUser().getIdLong(), role.getIdLong())
                );
                event.getGuild().addRoleToMember(event.getUser(), role).queue();
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getMember() == null) return;

        if (DebugLogging.isEventEnabled()) //noinspection LoggingSimilarMessage
            debugLogger.debug("Retrieving Message for ReactionUpdatedEvent.");
        Message message = new InternalMessage(event.retrieveMessage().complete());

        if (DebugLogging.isEventEnabled())//noinspection LoggingSimilarMessage
            debugLogger.debug("Firing a ReactionUpdatedEvent.");
        eventManager.callEvent(new InternalReactionUpdatedEvent(
                new InternalMember(event.getMember()),
                message,
                new InternalReaction(event.getReaction(), message),
                InternalReactionAction.INCREASED
        ));
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getMember() == null) return;

        if (DebugLogging.isEventEnabled())//noinspection LoggingSimilarMessage
            debugLogger.debug("Retrieving Message for ReactionUpdatedEvent.");
        Message message = new InternalMessage(event.retrieveMessage().complete());

        if (DebugLogging.isEventEnabled())//noinspection LoggingSimilarMessage
            debugLogger.debug("Firing a ReactionUpdatedEvent.");
        eventManager.callEvent(new InternalReactionUpdatedEvent(
                new InternalMember(event.getMember()),
                message,
                new InternalReaction(event.getReaction(), message),
                event.getReaction().retrieveUsers().complete().isEmpty() ? InternalReactionAction.REMOVED : InternalReactionAction.DECREASED
        ));
    }

    record RoleModificationLoopFix(boolean add, long affectedUser, long affectedRole) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            RoleModificationLoopFix that = (RoleModificationLoopFix) o;
            return add == that.add && affectedUser == that.affectedUser && affectedRole == that.affectedRole;
        }

        @Override
        public int hashCode() {
            return Objects.hash(add, affectedUser, affectedRole);
        }
    }
}
