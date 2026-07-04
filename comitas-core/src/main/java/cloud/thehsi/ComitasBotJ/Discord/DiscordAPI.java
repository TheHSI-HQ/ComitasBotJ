package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageSentEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleAddedEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleRemovedEvent;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalBotConnectEvent;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalMessageSentEvent;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalUserRoleAddedEvent;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalUserRoleRemovedEvent;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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

public class DiscordAPI extends ListenerAdapter {
    final JDA api;
    final EventManager eventManager;
    final ServerConfig.ParsedServerConfig config;
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".DiscordAPI");

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

    final List<RoleModificationLoopFix> roleModificationLoopFixList = new ArrayList<>();

    public DiscordAPI(String BOT_TOKEN, ServerConfig.ParsedServerConfig config, EventManager eventManager) {
        api = JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(this)
                .build();

        this.eventManager = eventManager;
        this.config = config;
    }

    public JDA getAPI() {
        return api;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageSentEvent messageSentEvent = new InternalMessageSentEvent(event);

        eventManager.callEvent(messageSentEvent);

        if (messageSentEvent.isDelete()) event.getMessage().delete().queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        if (!config.botActivityName.get().isBlank())
            api.getPresence().setActivity(
                    Activity.watching(config.botActivityName.get())
            );

        eventManager.callEvent(new InternalBotConnectEvent(
                        api.getSelfUser()
        ));

        logger.info("Done ({}s)! For help, type \"help\"", Main.getRuntimeMS() / 1000d);
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
                event.getGuild().removeRoleFromMember(event.getUser(), role).queue();
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
}
