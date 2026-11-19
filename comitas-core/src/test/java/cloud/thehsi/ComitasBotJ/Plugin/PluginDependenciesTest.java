package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PluginDependenciesTest {
    private static PluginDependencies.Node<String> node(String name, String... dependencies) {
        Plugin.PluginMetadata metadata = new Plugin.PluginMetadata(name, "1.0", name + ".jar", "0.0.18",
                UUID.nameUUIDFromBytes(name.getBytes()), name);
        return new PluginDependencies.Node<>(name, metadata, Arrays.asList(dependencies));
    }

    private static List<String> names(PluginDependencies.Order<String> order) {
        return order.order().stream().map(PluginDependencies.Node::value).toList();
    }

    @Test
    void parsesDependencies() {
        Properties props = new Properties();
        props.setProperty("dependencies", " A, ,b ,A");
        assertEquals(List.of("A", "b"), PluginDependencies.parse(props));
        assertEquals(List.of(), PluginDependencies.parse(new Properties()));
    }

    @Test
    void rejectsControlCharacters() {
        Properties props = new Properties();
        props.setProperty("dependencies", "Evil\u001B[2J");
        assertThrows(RuntimeException.class, () -> PluginDependencies.parse(props));
    }

    @Test
    void loadsDependenciesFirst() {
        var a = node("A", "B");
        var b = node("B", "c"); // names are case insensitive
        var c = node("C");
        var d = node("D");
        List<PluginDependencies.Node<String>> nodes = List.of(a, b, c, d);

        PluginDependencies.Order<String> order = PluginDependencies.order(nodes, nodes);
        assertEquals(List.of("C", "B", "A", "D"), names(order));
        assertTrue(order.circular().isEmpty());
    }

    @Test
    void matchesUuids() {
        var b = node("B");
        var a = node("A", b.metadata().uuid().toString().toUpperCase());
        List<PluginDependencies.Node<String>> nodes = List.of(a, b);
        assertEquals(List.of("B", "A"), names(PluginDependencies.order(nodes, nodes)));
    }

    @Test
    void onlyContainsTheRootsAndTheirDependencies() {
        var a = node("A", "B");
        var b = node("B");
        var c = node("C");
        assertEquals(List.of("B", "A"), names(PluginDependencies.order(List.of(a, b, c), List.of(a))));
    }

    @Test
    void ignoresDependenciesWhichAreNoNode() {
        var a = node("A", "Missing");
        assertEquals(List.of("A"), names(PluginDependencies.order(List.of(a), List.of(a))));
    }

    @Test
    void detectsCycles() {
        var a = node("A", "B");
        var b = node("B", "C");
        var c = node("C", "A");
        var d = node("D", "D");
        var e = node("E");
        List<PluginDependencies.Node<String>> nodes = List.of(a, b, c, d, e);

        PluginDependencies.Order<String> order = PluginDependencies.order(nodes, nodes);
        assertEquals(5, order.order().size());
        assertEquals("circular dependency A -> B -> C -> A", order.circular().get(a));
        assertTrue(order.circular().containsKey(b));
        assertTrue(order.circular().containsKey(c));
        assertEquals("circular dependency D -> D", order.circular().get(d));
        assertFalse(order.circular().containsKey(e));
    }
}
