package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageSentEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleAddedEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleRemovedEvent;
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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DiscordAPI extends ListenerAdapter {
    static JDA api;
    final EventManager eventManager;
    final InternalCommandRegistry commandRegistry;
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".DiscordAPI");
    final List<RoleModificationLoopFix> roleModificationLoopFixList = new ArrayList<>();
    private final String BOT_TOKEN;
    boolean firstStartup = true;

    public DiscordAPI(String BOT_TOKEN, EventManager eventManager, InternalCommandRegistry commandRegistry) {
        this.BOT_TOKEN = BOT_TOKEN;

        this.eventManager = eventManager;
        this.commandRegistry = commandRegistry;

        connect();
    }

    public static JDA api() {
        return api;
    }

    private void connect() {
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
        if (api != null) {
            api.shutdown();

            if (!api.awaitShutdown(10, TimeUnit.SECONDS)) {
                api.shutdownNow();
            }
        }

        connect();
        api.awaitReady();
    }

    public JDA getAPI() {
        return api;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageSentEvent messageSentEvent = new InternalMessageSentEvent(event);

        eventManager.callEvent(messageSentEvent);

        if (messageSentEvent.isDelete()) event.getMessage().delete().queue(ignored -> {
        }, error -> {
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        commandRegistry.handleCommand(event);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        if (!Main.conf().botActivityName.get().isBlank())
            api.getPresence().setActivity(
                    Activity.watching(Main.conf().botActivityName.get())
            );

        commandRegistry.setDiscordApi(this);

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
                roleModificationLoopFixList.remove(new RoleModificationLoopFix(true, event.getUser().getIdLong(), role.getIdLong()));
                continue;
            }

            UserRoleAddedEvent userRoleAddedEvent = new InternalUserRoleAddedEvent(
                    new InternalMember(event.getMember()),
                    new InternalRole(role)
            );

            eventManager.callEvent(userRoleAddedEvent);

            if (userRoleAddedEvent.willUndo()) {
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
                roleModificationLoopFixList.remove(new RoleModificationLoopFix(false, event.getUser().getIdLong(), role.getIdLong()));
                continue;
            }

            UserRoleRemovedEvent userRoleRemovedEvent = new InternalUserRoleRemovedEvent(
                    new InternalMember(event.getMember()),
                    new InternalRole(role)
            );

            eventManager.callEvent(userRoleRemovedEvent);

            if (userRoleRemovedEvent.willUndo()) {
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

        Message message = new InternalMessage(event.retrieveMessage().complete());

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

        Message message = new InternalMessage(event.retrieveMessage().complete());

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
