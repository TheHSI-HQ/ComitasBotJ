package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.Discord.BotPermissionSnapshot;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PluginDiscordPermissionsTest {
    @Test
    void parsesDeclaredPermissions() {
        Properties props = properties(Map.of(
                "discord-permissions", "send_messages, EMBED-LINKS,unknown_permission",
                "discord-permission.send_messages.required", "true",
                "discord-permission.embed_links.reason", "  Shows the weather  ",
                // Required permissions are requested, even if they aren't listed
                "discord-permission.ban_members.required", "yes"));

        LinkedHashMap<Permission, PluginDiscordPermissions.Request> requests =
                PluginDiscordPermissions.parseRequests(props, "Test");

        assertEquals(List.of(Permission.SEND_MESSAGES, Permission.EMBED_LINKS, Permission.BAN_MEMBERS),
                List.copyOf(requests.keySet()));
        assertTrue(requests.get(Permission.SEND_MESSAGES).required());
        assertFalse(requests.get(Permission.EMBED_LINKS).required());
        assertEquals("Shows the weather", requests.get(Permission.EMBED_LINKS).reason());
        assertTrue(requests.get(Permission.BAN_MEMBERS).required());
    }

    @Test
    void anEmptyListIsADeclaration() {
        assertTrue(PluginDiscordPermissions.declares(properties(Map.of("discord-permissions", ""))));
        assertTrue(PluginDiscordPermissions.declares(properties(Map.of("discord-permission.kick_members.required", "true"))));
        assertFalse(PluginDiscordPermissions.declares(properties(Map.of("permissions", "network"))));
    }

    @Test
    void refusesOnlyIfNoGuildGrantsARequiredPermission() {
        BotPermissionSnapshot bot = new BotPermissionSnapshot(Map.of(
                "Main (1)", Set.of(Permission.SEND_MESSAGES, Permission.BAN_MEMBERS),
                "Locked (2)", Set.of(Permission.SEND_MESSAGES)));
        List<PluginDiscordPermissions.Request> requests = List.of(
                new PluginDiscordPermissions.Request(Permission.SEND_MESSAGES, null, true),
                new PluginDiscordPermissions.Request(Permission.BAN_MEMBERS, null, true),
                new PluginDiscordPermissions.Request(Permission.MANAGE_ROLES, null, false));

        PluginDiscordPermissions.BotCheck check = PluginDiscordPermissions.check(requests, bot);
        assertFalse(check.refuses(), "BAN_MEMBERS is granted in one guild");
        assertEquals(List.of(Permission.MANAGE_ROLES), check.missingOptional());
        assertEquals(Map.of(Permission.BAN_MEMBERS, List.of("Locked (2)")), check.lackingGuilds());

        PluginDiscordPermissions.BotCheck missing = PluginDiscordPermissions.check(
                List.of(new PluginDiscordPermissions.Request(Permission.KICK_MEMBERS, null, true)), bot);
        assertTrue(missing.refuses());
        assertEquals(List.of(Permission.KICK_MEMBERS), missing.missingRequired());
    }

    @Test
    void aBotInNoGuildSatisfiesNothing() {
        PluginDiscordPermissions.BotCheck check = PluginDiscordPermissions.check(
                List.of(new PluginDiscordPermissions.Request(Permission.SEND_MESSAGES, null, true)),
                new BotPermissionSnapshot(Map.of()));
        assertTrue(check.refuses());
    }

    @Test
    void parsesPromptAnswersForDiscordPermissions() {
        List<PluginSecurity.Choice<Permission>> choices = List.of(
                choice(Permission.SEND_MESSAGES, true), choice(Permission.EMBED_LINKS, false), choice(Permission.BAN_MEMBERS, false));

        assertEquals(Set.of(Permission.SEND_MESSAGES, Permission.EMBED_LINKS, Permission.BAN_MEMBERS),
                Objects.requireNonNull(PluginSecurity.parseAnswer("a", choices)).granted());
        assertEquals(Set.of(Permission.SEND_MESSAGES), Objects.requireNonNull(PluginSecurity.parseAnswer("o", choices)).granted());
        assertEquals(Set.of(Permission.SEND_MESSAGES, Permission.BAN_MEMBERS), Objects.requireNonNull(PluginSecurity.parseAnswer("1, 3", choices)).granted());
        assertTrue(Objects.requireNonNull(PluginSecurity.parseAnswer("n", choices)).granted().isEmpty());
        assertNull(PluginSecurity.parseAnswer("4", choices));
        assertNull(PluginSecurity.parseAnswer("maybe", choices));
    }

    @Test
    void formatsPermissionNames() {
        assertEquals("Send Messages In Threads", PluginSecurity.displayName(Permission.SEND_MESSAGES_IN_THREADS));
        assertEquals("send_messages", PluginDiscordPermissions.key(Permission.SEND_MESSAGES));
        assertEquals(Permission.MANAGE_GUILD, PluginDiscordPermissions.fromKey(" manage-guild "));
        assertNull(PluginDiscordPermissions.fromKey("manage_server"));
    }

    private static PluginSecurity.Choice<Permission> choice(Permission permission, boolean required) {
        String key = PluginDiscordPermissions.key(permission);
        return new PluginSecurity.Choice<>(permission, key, "discord-permission." + key, key, null, required, null, false);
    }

    private static Properties properties(Map<String, String> values) {
        Properties props = new Properties();
        props.putAll(values);
        return props;
    }
}
