package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.ReactionAction;
import cloud.thehsi.ComitasBotJ.API.Event.Events.*;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Commands.InternalCommandRegistry;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.Reaction.InternalReaction;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Event.Events.*;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscordAPIListeners extends ListenerAdapter {
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".DiscordAPIListeners");
    final Logger debugLogger = DebugLogging.getLogger();
    final DiscordAPI api;
    final EventManager eventManager;
    final InternalCommandRegistry commandRegistry;
    boolean firstStartup = true;

    final List<String> undoLoopFixList = Collections.synchronizedList(new ArrayList<>());

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
            if (resolveUndoLoopPrevention(UserRoleAddedEvent.class, event.getUser().getIdLong(), role.getIdLong()))
                continue;

            UserRoleAddedEvent userRoleAddedEvent = new InternalUserRoleAddedEvent(
                    new InternalMember(event.getMember()),
                    new InternalRole(role)
            );

            if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserRoleAddedEvent.");
            eventManager.callEvent(userRoleAddedEvent);

            if (userRoleAddedEvent.willUndo()) {
                addUndoLoopPrevention(UserRoleRemovedEvent.class, event.getUser().getIdLong(), role.getIdLong());
                event.getGuild().removeRoleFromMember(event.getUser(), role).queue(ignored -> {
                }, error -> {
                });
            }
        }
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        for (Role role : event.getRoles()) {
            if (resolveUndoLoopPrevention(UserRoleRemovedEvent.class, event.getUser().getIdLong(), role.getIdLong()))
                continue;

            UserRoleRemovedEvent userRoleRemovedEvent = new InternalUserRoleRemovedEvent(
                    new InternalMember(event.getMember()),
                    new InternalRole(role)
            );

            if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserRoleRemovedEvent.");
            eventManager.callEvent(userRoleRemovedEvent);

            if (userRoleRemovedEvent.willUndo()) {
                addUndoLoopPrevention(UserRoleAddedEvent.class, event.getUser().getIdLong(), role.getIdLong());
                event.getGuild().addRoleToMember(event.getUser(), role).queue();
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getMember() == null) return;

        InternalMessage message = new InternalMessage(event.retrieveMessage().complete());

        ReactionUpdatedEvent reactionUpdatedEvent = new InternalReactionUpdatedEvent(
                new InternalMember(event.getMember()),
                message,
                new InternalReaction(event.getReaction(), message),
                ReactionAction.INCREASED
        );

        if (DebugLogging.isEventEnabled())//noinspection LoggingSimilarMessage
            debugLogger.debug("Firing a ReactionUpdatedEvent.");
        eventManager.callEvent(reactionUpdatedEvent);

        if (reactionUpdatedEvent.willUndo()) {
            addUndoLoopPrevention(ReactionUpdatedEvent.class, "remove", event.getMember().getIdLong(), event.getReaction().getMessageIdLong(), event.getEmoji().getName());
            message.message.removeReaction(event.getReaction().getEmoji(), event.getMember().getUser()).queue();
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getMember() == null) return;

        if (resolveUndoLoopPrevention(ReactionUpdatedEvent.class, "remove", event.getMember().getIdLong(), event.getReaction().getMessageIdLong(), event.getEmoji().getName()))
            return;

        Message message = new InternalMessage(event.retrieveMessage().complete());

        ReactionUpdatedEvent reactionUpdatedEvent = new InternalReactionUpdatedEvent(
                new InternalMember(event.getMember()),
                message,
                new InternalReaction(event.getReaction(), message),
                event.getReaction().retrieveUsers().complete().isEmpty() ? ReactionAction.REMOVED : ReactionAction.DECREASED
        );

        if (DebugLogging.isEventEnabled())//noinspection LoggingSimilarMessage
            debugLogger.debug("Firing a ReactionUpdatedEvent.");
        eventManager.callEvent(reactionUpdatedEvent);

        if (reactionUpdatedEvent.willUndo()) {
            logger.warn("Cannot undo ReactionUpdatedEvent with a ReactionAction of DECREASED / REMOVED");
        }
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
        if (resolveUndoLoopPrevention(UserBannedEvent.class, event.getUser().getIdLong(), event.getGuild().getIdLong()))
            return;

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserBannedEvent.");

        UserBannedEvent userBannedEvent = new InternalUserBannedEvent(event);

        eventManager.callEvent(userBannedEvent);

        if (userBannedEvent.willUndo()) {
            addUndoLoopPrevention(UserUnbannedEvent.class, event.getUser().getIdLong(), event.getGuild().getIdLong());
            event.getGuild().unban(event.getUser()).queue();
        }
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        if (resolveUndoLoopPrevention(UserUnbannedEvent.class, event.getUser().getIdLong(), event.getGuild().getIdLong()))
            return;

        UserUnbannedEvent userUnbannedEvent = new InternalUserUnbannedEvent(event);

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserUnbannedEvent.");
        eventManager.callEvent(userUnbannedEvent);

        if (userUnbannedEvent.willUndo()) {
            addUndoLoopPrevention(UserBannedEvent.class, event.getUser().getIdLong(), event.getGuild().getIdLong());
            event.getGuild().ban(event.getUser(), 0, TimeUnit.SECONDS).queue();
        }
    }

    @Override
    public void onGuildAuditLogEntryCreate(@NotNull GuildAuditLogEntryCreateEvent event) {
        //noinspection SwitchStatementWithTooFewBranches // Will be expanded later
        switch (event.getEntry().getType()) {
            case ActionType.KICK:
                if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserKickedEvent.");
                eventManager.callEvent(new InternalUserKickedEvent(
                        new InternalUser(event.getEntry().getUser()),
                        new InternalGuild(event.getGuild())
                ));
                break;
        }
    }

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserChangeUserNameEvent.");
        eventManager.callEvent(new InternalUserChangeUserNameEvent(
                new InternalUser(event.getUser()),
                event.getOldName(),
                event.getNewName()
        ));
    }

    @Override
    public void onUserUpdateGlobalName(@NotNull UserUpdateGlobalNameEvent event) {
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserChangeGlobalDisplayNameEvent.");
        eventManager.callEvent(new InternalUserChangeGlobalDisplayNameEvent(
                new InternalUser(event.getUser()),
                event.getOldGlobalName(),
                event.getNewGlobalName()
        ));
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        if (resolveUndoLoopPrevention(UserChangeGuildDisplayNameEvent.class, event.getUser().getIdLong(), event.getGuild().getIdLong(), event.getNewNickname()))
            return;

        UserChangeGuildDisplayNameEvent userChangeGuildDisplayNameEvent = new InternalUserChangeGuildDisplayNameEvent(
                new InternalMember(event.getMember()),
                new InternalGuild(event.getGuild()),
                event.getOldNickname(),
                event.getNewNickname()
        );

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a UserChangeGuildDisplayNameEvent.");
        eventManager.callEvent(userChangeGuildDisplayNameEvent);

        if (userChangeGuildDisplayNameEvent.willUndo()) {
            addUndoLoopPrevention(UserChangeGuildDisplayNameEvent.class, event.getUser().getIdLong(), event.getGuild().getIdLong(), event.getOldNickname());
            event.getGuild().modifyNickname(event.getMember(), event.getOldNickname()).queue();
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a MessageDeletedEvent.");
        eventManager.callEvent(new InternalMessageDeletedEvent(event));
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Firing a MessageEditedEvent.");
        MessageEditedEvent messageEditedEvent = new InternalMessageEditedEvent(event);

        eventManager.callEvent(messageEditedEvent);

        if (messageEditedEvent.markedForDeletion()) event.getMessage().delete().queue(ignored -> {
        }, error -> {
        });
        super.onMessageUpdate(event);
    }

    /*
     * Helpers
     */

    private boolean resolveUndoLoopPrevention(Class<? extends UndoableEvent> clazz, Object... args) {
        StringBuilder identifier = new StringBuilder(clazz.getName());
        for (@Nullable Object arg : args) {
            identifier.append(";").append(arg == null ? "null" : arg.hashCode());
        }
        if (undoLoopFixList.remove(identifier.toString())) {
            if (DebugLogging.isEventEnabled())
                debugLogger.debug("Ignoring event {}, prevented by UndoLoopFix", clazz.getSimpleName());
            return true;
        }
        return false;
    }

    private void addUndoLoopPrevention(Class<? extends UndoableEvent> clazz, Object... args) {
        StringBuilder identifier = new StringBuilder(clazz.getName());
        for (@Nullable Object arg : args) {
            identifier.append(";").append(arg == null ? "null" : arg.hashCode());
        }
        if (DebugLogging.isEventEnabled())
            debugLogger.debug("Adding a UndoLoopFix, as event {} was marked to be undone", clazz.getSimpleName());
        undoLoopFixList.add(identifier.toString());
    }
}
