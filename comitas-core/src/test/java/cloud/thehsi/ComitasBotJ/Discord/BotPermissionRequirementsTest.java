package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.VoiceChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Invite;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission;
import cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission.Scope;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.*;

class BotPermissionRequirementsTest {
    @Test
    void annotationsAreConsistent() throws Exception {
        List<String> problems = new ArrayList<>();
        int annotated = 0;

        for (Class<?> clazz : apiClasses()) {
            for (Method method : clazz.getDeclaredMethods()) {
                RequiresBotPermission[] annotations = method.getAnnotationsByType(RequiresBotPermission.class);
                if (annotations.length == 0)
                    continue;
                annotated++;
                String name = clazz.getSimpleName() + "#" + method.getName();

                for (RequiresBotPermission a : annotations) {
                    Set<Permission> value = Set.of(a.value());
                    Set<Permission> anyOf = Set.of(a.anyOf());
                    Set<Permission> conditional = Set.of(a.conditional());
                    if (value.isEmpty() && anyOf.isEmpty() && conditional.isEmpty())
                        problems.add(name + " lists no permission");
                    if (Stream.of(value, anyOf, conditional).anyMatch(s -> s.contains(Permission.VIEW_CHANNEL)))
                        problems.add(name + " lists VIEW_CHANNEL, it is implied");
                    if (value.contains(Permission.ADMINISTRATOR) || anyOf.contains(Permission.ADMINISTRATOR))
                        problems.add(name + " requires ADMINISTRATOR, list the specific permissions instead");
                    if (anyOf.size() == 1)
                        problems.add(name + " lists a single anyOf permission, use value instead");
                    if (!Collections.disjoint(value, anyOf) || !Collections.disjoint(value, conditional)
                            || !Collections.disjoint(anyOf, conditional))
                        problems.add(name + " lists a permission in more than one of value, anyOf and conditional");
                    if (a.scope() == Scope.TARGET_CHANNEL && BotPermissionRequirements.targetChannelParameter(method) < 0)
                        problems.add(name + " uses TARGET_CHANNEL without a channel parameter");
                }
            }
        }

        assertTrue(annotated > 50, "Found only " + annotated + " annotated API methods");
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    @Test
    void appliesImplicitRules() throws Exception {
        Method send = MessageChannel.class.getMethod("sendMessage", Component.class);
        BotPermissionRequirements.Requirement requirement = BotPermissionRequirements.of(send).getFirst();

        assertEquals(Set.of(Permission.SEND_MESSAGES, Permission.VIEW_CHANNEL), requirement.requiredIn(false));
        assertEquals(Set.of(Permission.SEND_MESSAGES_IN_THREADS, Permission.VIEW_CHANNEL), requirement.requiredIn(true));

        Method ban = Guild.class.getMethod("ban", Member.class);
        assertEquals(Set.of(Permission.BAN_MEMBERS), BotPermissionRequirements.of(ban).getFirst().requiredIn(false));
    }

    @Test
    void readsRepeatedAnnotations() throws Exception {
        Method moveTo = Member.class.getMethod("moveTo", VoiceChannel.class);
        List<BotPermissionRequirements.Requirement> requirements = BotPermissionRequirements.of(moveTo);

        assertEquals(List.of(Scope.CHANNEL, Scope.TARGET_CHANNEL),
                requirements.stream().map(BotPermissionRequirements.Requirement::scope).toList());
        assertEquals(0, BotPermissionRequirements.targetChannelParameter(moveTo));
    }

    @Test
    void anyOfNeedsOneAlternative() throws Exception {
        BotPermissionRequirements.Requirement delete = BotPermissionRequirements.of(Invite.class.getMethod("delete")).getFirst();

        assertEquals(Set.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_GUILD), delete.anyOfIn(false));
        assertTrue(delete.satisfiedBy(Set.of(Permission.VIEW_CHANNEL, Permission.MANAGE_GUILD), false));
        assertTrue(delete.satisfiedBy(Set.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), false));
        assertFalse(delete.satisfiedBy(Set.of(Permission.VIEW_CHANNEL), false));
        assertFalse(delete.satisfiedBy(Set.of(Permission.MANAGE_GUILD), false), "VIEW_CHANNEL is implied");
        assertTrue(delete.satisfiedBy(Set.of(Permission.ADMINISTRATOR), false));
    }

    @Test
    void conditionalPermissionsDontBlockCalls() throws Exception {
        Method send = MessageChannel.class.getMethod("sendMessage", MessageData.class);
        BotPermissionRequirements.Requirement requirement = BotPermissionRequirements.of(send).getFirst();

        assertTrue(requirement.conditionalIn(false).contains(Permission.EMBED_LINKS));
        assertTrue(requirement.satisfiedBy(Set.of(Permission.VIEW_CHANNEL, Permission.SEND_MESSAGES), false));
        assertFalse(requirement.satisfiedBy(Set.of(Permission.VIEW_CHANNEL, Permission.SEND_MESSAGES), true));
    }

    @Test
    void pluginLimitsIgnoreChannelContext() throws Exception {
        BotPermissionRequirements.Requirement send = BotPermissionRequirements.of(
                MessageChannel.class.getMethod("sendMessage", Component.class)).getFirst();
        // VIEW_CHANNEL is implied, threads are unknown without the receiver
        assertTrue(send.permittedBy(Set.of(Permission.SEND_MESSAGES)));
        assertTrue(send.permittedBy(Set.of(Permission.SEND_MESSAGES_IN_THREADS)));
        assertEquals(Set.of(Permission.SEND_MESSAGES), send.missingFrom(Set.of(Permission.EMBED_LINKS)));

        BotPermissionRequirements.Requirement delete = BotPermissionRequirements.of(Invite.class.getMethod("delete")).getFirst();
        assertEquals(Set.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_GUILD), delete.missingFrom(Set.of()));
        assertTrue(delete.permittedBy(Set.of(Permission.MANAGE_CHANNEL)));
        assertTrue(delete.permittedBy(Set.of(Permission.ADMINISTRATOR)));
    }

    @Test
    void unannotatedMethodsRequireNothing() throws Exception {
        assertTrue(BotPermissionRequirements.of(Guild.class.getMethod("getName")).isEmpty());
    }

    private static List<Class<?>> apiClasses() throws Exception {
        Path location = Path.of(Guild.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> names = new ArrayList<>();

        if (Files.isDirectory(location)) {
            try (Stream<Path> files = Files.walk(location)) {
                files.filter(p -> p.toString().endsWith(".class"))
                        .forEach(p -> names.add(location.relativize(p).toString().replace('\\', '/')));
            }
        } else {
            try (JarFile jar = new JarFile(location.toFile())) {
                jar.stream().map(ZipEntry::getName).filter(n -> n.endsWith(".class")).forEach(names::add);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        List<Class<?>> classes = new ArrayList<>();
        for (String name : names) {
            if (!name.startsWith("cloud/thehsi/ComitasBotJ/API/") || name.endsWith("module-info.class"))
                continue;
            String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
            classes.add(Class.forName(className, false, Guild.class.getClassLoader()));
        }
        return classes;
    }
}
