package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the bundled changelogs against the current API.
 */
class ChangelogRepositoryTest {
    private final ChangelogRepository repository = ChangelogRepository.bundled();

    @Test
    void everyReleaseHasAChangelog() {
        String version = System.getProperty("comitas.version");
        String previous = System.getProperty("comitas.previous.version");
        assertNotNull(version, "Run the tests through Maven");

        ApiChangelog changelog = repository.release(version);
        assertNotNull(changelog, "Missing changelog for " + version + ": add comitas-compatibility/changelog/" + version
                + ".json (with \"changes\": [] if nothing broke) and list it in index.txt");
        assertEquals(previous, changelog.from(),
                "The changelog of " + version + " must start at the last release (comitas.previous.version)");
    }

    @Test
    void everyChangelogFileIsListed() throws IOException, URISyntaxException {
        URL index = getClass().getClassLoader().getResource(ChangelogRepository.INDEX);
        assertNotNull(index);
        if (!index.getProtocol().equals("file")) return;

        List<String> listed = ChangelogRepository.readIndex(getClass().getClassLoader());
        try (Stream<Path> files = Files.list(Path.of(index.toURI()).getParent())) {
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).toList())
                assertTrue(listed.contains(file.getFileName().toString()), file.getFileName() + " is missing in index.txt");
        }
    }

    @Test
    void shimsMatchTheirChanges() throws ClassNotFoundException {
        List<String> problems = new ArrayList<>();
        for (ApiChangelog changelog : repository.changelogs()) {
            CompatibilityPlan rest = Objects.requireNonNull(repository.plan(changelog.from(), latest()));
            for (Change change : changelog.changes()) {
                if (change.shim() == null) continue;

                // Later class renames are applied to the redirected call as well
                String expected = mapDescriptor(rest, change.shimDescriptor());
                Class<?> shim = Class.forName(change.shimOwner().replace('/', '.'));
                Method method = Stream.of(shim.getMethods())
                        .filter(m -> m.getName().equals(change.shimName()) && Type.getMethodDescriptor(m).equals(expected))
                        .findFirst().orElse(null);

                if (method == null)
                    problems.add(change.id() + ": " + shim.getName() + " has no public method " + change.shimName() + expected);
                else if (!Modifier.isStatic(method.getModifiers()))
                    problems.add(change.id() + ": " + method + " must be static");
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    @Test
    void newMembersExist() {
        List<String> problems = new ArrayList<>();
        List<ApiChangelog> changelogs = repository.changelogs();
        for (int i = 0; i < changelogs.size(); i++) {
            CompatibilityPlan rest = Objects.requireNonNull(repository.plan(changelogs.get(i).from(), latest()));
            List<Change> later = changelogs.subList(i + 1, changelogs.size()).stream()
                    .flatMap(c -> c.changes().stream()).toList();

            for (Change change : changelogs.get(i).changes()) {
                switch (change.kind()) {
                    case CLASS_RENAMED -> {
                        String target = rest.mapClassName(change.owner());
                        if (load(target) == null) problems.add(change.id() + ": " + target + " doesn't exist");
                    }
                    case METHOD_RENAMED, FIELD_RENAMED -> {
                        String owner = rest.mapClassName(change.owner());
                        String descriptor = mapDescriptor(rest, Objects.requireNonNull(change.descriptor()));
                        boolean renamedAgain = later.stream().anyMatch(c -> c.name() != null
                                && c.name().equals(change.newName()) && rest.mapClassName(c.owner()).equals(owner));
                        if (!renamedAgain) {
                            assertNotNull(change.newName());
                            if (!hasMember(owner, change.newName(), descriptor, change.kind() == ChangeKind.FIELD_RENAMED))
                                problems.add(change.id() + ": " + owner + " has no member " + change.newName() + " " + descriptor);
                        }
                    }
                    case ABSTRACT_METHOD_CHANGED -> {
                        String owner = rest.mapClassName(change.owner());
                        String descriptor = mapDescriptor(rest, Objects.requireNonNull(change.newDescriptor()));
                        assertNotNull(change.name());
                        if (!hasMember(owner, change.name(), descriptor, false))
                            problems.add(change.id() + ": " + owner + " has no method " + change.name() + descriptor);
                    }
                    default -> {
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    private String latest() {
        return repository.changelogs().getLast().to();
    }

    static String mapDescriptor(CompatibilityPlan plan, String descriptor) {
        Type type = Type.getType(descriptor);
        if (type.getSort() != Type.METHOD) return map(plan, type).getDescriptor();
        Type[] args = Stream.of(type.getArgumentTypes()).map(t -> map(plan, t)).toArray(Type[]::new);
        return Type.getMethodDescriptor(map(plan, type.getReturnType()), args);
    }

    private static Type map(CompatibilityPlan plan, Type type) {
        if (type.getSort() == Type.OBJECT) return Type.getObjectType(plan.mapClassName(type.getInternalName()));
        if (type.getSort() == Type.ARRAY)
            return Type.getType("[".repeat(type.getDimensions()) + map(plan, type.getElementType()).getDescriptor());
        return type;
    }

    private static @Nullable Class<?> load(String internalName) {
        try {
            return Class.forName(internalName.replace('/', '.'), false, ChangelogRepositoryTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasMember(String owner, String name, String descriptor, boolean field) {
        for (Class<?> c = load(owner); c != null; c = c.getSuperclass()) {
            if (field) {
                for (var f : c.getDeclaredFields())
                    if (f.getName().equals(name) && Type.getDescriptor(f.getType()).equals(descriptor)) return true;
            } else {
                for (var m : c.getMethods())
                    if (m.getName().equals(name) && Type.getMethodDescriptor(m).equals(descriptor)) return true;
                for (var m : c.getDeclaredMethods())
                    if (m.getName().equals(name) && Type.getMethodDescriptor(m).equals(descriptor)) return true;
            }
        }
        return false;
    }
}
