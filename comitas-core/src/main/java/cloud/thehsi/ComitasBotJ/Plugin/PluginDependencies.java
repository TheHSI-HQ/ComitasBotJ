package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Plugin dependencies, declared in {@code plugin.properties}:
 * <pre>
 * # Uuids (preferred) or names of plugins, separated by commas
 * dependencies=3f1c0b6e-8f0a-4c1e-9a57-2b8c3c0d9e11, OtherPlugin
 * </pre>
 * A plugin is only loaded, if all its dependencies are loaded, they are always loaded (and enabled) before it.
 */
final class PluginDependencies {
    @NotNull
    static final String KEY = "dependencies";

    private PluginDependencies() {
    }

    /**
     * @return The declared dependencies, uuids or names, without duplicates
     */
    @NotNull
    static List<String> parse(@NotNull Properties props) {
        String value = props.getProperty(KEY);
        if (value == null) return List.of();

        Set<String> dependencies = new LinkedHashSet<>();
        for (String entry : value.split(",")) {
            String dependency = entry.strip();
            if (dependency.isEmpty()) continue;
            if (dependency.length() > 128)
                throw new RuntimeException("Plugin has a too long entry in " + KEY + " in plugin.properties (max. 128 characters)");
            for (int i = 0; i < dependency.length(); i++)
                if (Character.isISOControl(dependency.charAt(i)) || Character.getType(dependency.charAt(i)) == Character.FORMAT)
                    throw new RuntimeException("Plugin has an illegal character in " + KEY + " in plugin.properties");
            dependencies.add(dependency);
        }
        return List.copyOf(dependencies);
    }

    /**
     * Dependencies are matched by uuid or name, both case insensitive.
     */
    static boolean matches(@NotNull String dependency, @NotNull Plugin.PluginMetadata metadata) {
        return dependency.equalsIgnoreCase(metadata.uuid().toString()) || dependency.equalsIgnoreCase(metadata.name());
    }

    /**
     * Finds the plugin a dependency refers to, uuids take precedence over names.
     */
    @Nullable
    static <T> T find(@NotNull String dependency, @NotNull Collection<T> candidates,
                      @NotNull Function<T, Plugin.PluginMetadata> metadata) {
        for (T candidate : candidates) {
            Plugin.PluginMetadata m = metadata.apply(candidate);
            if (m != null && dependency.equalsIgnoreCase(m.uuid().toString())) return candidate;
        }
        for (T candidate : candidates) {
            Plugin.PluginMetadata m = metadata.apply(candidate);
            if (m != null && dependency.equalsIgnoreCase(m.name())) return candidate;
        }
        return null;
    }

    /**
     * Orders nodes so dependencies come first. Dependencies, which aren't one of the nodes
     * (already loaded or missing), are ignored, they are checked when the plugin is loaded.
     *
     * @param nodes All loadable jars
     * @param roots The nodes, which should be loaded, the result also contains their (indirect) dependencies
     */
    @NotNull
    static <T> Order<T> order(@NotNull List<Node<T>> nodes, @NotNull List<Node<T>> roots) {
        List<Node<T>> order = new ArrayList<>();
        Map<Node<T>, String> circular = new LinkedHashMap<>();
        Set<Node<T>> done = new HashSet<>();
        Deque<Node<T>> stack = new ArrayDeque<>();
        for (Node<T> root : roots) visit(root, nodes, order, circular, done, stack);
        return new Order<>(order, circular);
    }

    private static <T> void visit(@NotNull Node<T> node, @NotNull List<Node<T>> nodes, @NotNull List<Node<T>> order,
                                  @NotNull Map<Node<T>, String> circular, @NotNull Set<Node<T>> done,
                                  @NotNull Deque<Node<T>> stack) {
        if (done.contains(node)) return;
        if (stack.contains(node)) {
            // The stack is the path from a root to here, the cycle starts where the node was first visited
            List<Node<T>> path = new ArrayList<>(stack);
            Collections.reverse(path);
            List<Node<T>> cycle = path.subList(path.indexOf(node), path.size());
            List<String> names = new ArrayList<>(cycle.stream().map(PluginDependencies::name).toList());
            names.add(name(node));
            String chain = "circular dependency " + String.join(" -> ", names);
            for (Node<T> member : cycle) circular.putIfAbsent(member, chain);
            return;
        }

        stack.push(node);
        for (String dependency : node.dependencies()) {
            Node<T> target = find(dependency, nodes, Node::metadata);
            if (target != null) visit(target, nodes, order, circular, done, stack);
        }
        stack.pop();
        done.add(node);
        order.add(node);
    }

    @NotNull
    private static String name(@NotNull Node<?> node) {
        return node.metadata() == null ? String.valueOf(node.value()) : node.metadata().name();
    }

    /**
     * A jar, which may be loaded.
     *
     * @param metadata Null, if its plugin.properties couldn't be read, it fails on load anyway
     */
    record Node<T>(@NotNull T value, @Nullable Plugin.PluginMetadata metadata, @NotNull List<String> dependencies) {
    }

    /**
     * @param order    The nodes to load, each after the ones it depends on
     * @param circular Nodes, which (indirectly) depend on themselves, with the dependency chain. They are part of
     *                 {@code order}, but must not be loaded.
     */
    record Order<T>(@NotNull List<Node<T>> order, @NotNull Map<Node<T>, String> circular) {
    }
}
