package cloud.thehsi.ComitasBotJ.Discord;

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
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DiscordAPIListeners extends ListenerAdapter {
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".DiscordAPIListeners");
    final Logger debugLogger = DebugLogging.getLogger();
    final DiscordAPI api;
    final EventManager eventManager;
    final InternalCommandRegistry commandRegistry;
    boolean firstStartup = true;

    final List<UndoLoopFix> undoLoopFixList = Collections.synchronizedList(new ArrayList<>());

    public DiscordAPIListeners(DiscordAPI api, EventManager eventManager, InternalCommandRegistry commandRegistry) {
        this.eventManager = eventManager;
        this.commandRegistry = commandRegistry;
        this.api = api;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (DebugLogging.isEventEnabled())
            debugLogger.debug("Command {} got ran, passingto registry.", event.getName());
        commandRegistry.handleCommand(event);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Setting Bot Presence.");

        if (!Main.conf().botActivityName.get().isBlank())
            api.getAPI().getPresence().setActivity(
                    Activity.watching(Main.conf().botActivityName.get())
            );

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Updating Command Registry Discord API reference.");
        commandRegistry.setDiscordApi(api);

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a BotReadyEvent.");
        eventManager.callEvent(new InternalBotReadyEvent(
                api.getAPI().getSelfUser()
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
            if (undoLoopFixList.contains(
                    UndoLoopFix.create(true, event.getUser().getIdLong(), role.getIdLong())
            )) {
                if (DebugLogging.isEventEnabled()) //noinspection LoggingSimilarMessage
                    debugLogger.debug("Ignoring event for role {}, prevented by RoleModificationLoopFix", role.getName());
                undoLoopFixList.remove(UndoLoopFix.create(true, event.getUser().getIdLong(), role.getIdLong()));
                continue;
            }

            UserRoleAddedEvent userRoleAddedEvent = new InternalUserRoleAddedEvent(
                    new InternalMember(event.getMember()),
                    new InternalRole(role)
            );

            if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserRoleAddedEvent.");
            eventManager.callEvent(userRoleAddedEvent);

            if (userRoleAddedEvent.willUndo()) {
                if (DebugLogging.isEventEnabled())
                    debugLogger.debug("Adding a RoleModificationLopeFix, as role addition was marked to be undone");
                undoLoopFixList.add(
                        UndoLoopFix.create(false, event.getUser().getIdLong(), role.getIdLong())
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
            if (undoLoopFixList.contains(
                    UndoLoopFix.create(false, event.getUser().getIdLong(), role.getIdLong())
            )) {
                if (DebugLogging.isEventEnabled()) //noinspection LoggingSimilarMessage
                    debugLogger.debug("Ignoring event for role {}, prevented by RoleModificationLoopFix", role.getName());
                undoLoopFixList.remove(UndoLoopFix.create(false, event.getUser().getIdLong(), role.getIdLong()));
                continue;
            }

            UserRoleRemovedEvent userRoleRemovedEvent = new InternalUserRoleRemovedEvent(
                    new InternalMember(event.getMember()),
                    new InternalRole(role)
            );

            if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserRoleRemovedEvent.");
            eventManager.callEvent(userRoleRemovedEvent);

            if (userRoleRemovedEvent.willUndo()) {
                if (DebugLogging.isEventEnabled())
                    debugLogger.debug("Adding a RoleModificationLopeFix, as role removal was marked to be undone");
                undoLoopFixList.add(
                        UndoLoopFix.create(true, event.getUser().getIdLong(), role.getIdLong())
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
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserJoinGuildEvent.");

        eventManager.callEvent(new InternalUserJoinGuildEvent(event));
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserLeaveGuildEvent.");

        eventManager.callEvent(new InternalUserLeaveGuildEvent(event));
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserBannedEvent.");

        eventManager.callEvent(new InternalUserBannedEvent(event));
    }

    record UndoLoopFix(String identifier) {
        public static UndoLoopFix create(Object... args) {
            StringBuilder _identifier = new StringBuilder();
            for (Object arg : args) {
                _identifier.append(arg.hashCode()).append(";");
            }
            return new UndoLoopFix(_identifier.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            UndoLoopFix that = (UndoLoopFix) o;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return identifier.hashCode();
        }
    }
}
