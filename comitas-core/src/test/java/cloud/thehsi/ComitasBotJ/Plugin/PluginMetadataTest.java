package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataCoercion;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class PluginMetadataTest {
    private static Properties properties(String name) {
        Properties props = new Properties();
        props.setProperty("name", name);
        props.setProperty("version", "1.0.0");
        props.setProperty("api-target", "0.0.17");
        props.setProperty("uuid", " 3f1c0b6e-8f0a-4c1e-9a57-2b8c3c0d9e11 ");
        return props;
    }

    @Test
    void acceptsNamesWithSpaces() {
        Plugin.PluginMetadata metadata = Plugin.PluginMetadata.fromProperties(properties(" My Plugin "), "plugin.jar");
        assertEquals("My Plugin", metadata.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Evil\nPlugin", "Evil\u001B[2JPlugin", "Evil Plugin", "Evil‮Plugin", " "})
    void rejectsNamesThatCouldSpoofOutput(String name) {
        assertThrows(RuntimeException.class, () -> Plugin.PluginMetadata.fromProperties(properties(name), "plugin.jar"));
    }

    @Test
    void rejectsTooLongNames() {
        assertThrows(RuntimeException.class, () -> Plugin.PluginMetadata.fromProperties(properties("x".repeat(129)), "plugin.jar"));
    }

    @Test
    void coercesFloatsAndCharacters() {
        assertEquals(1.5f, PersistentDataCoercion.coerce(1.5, float.class));
        assertEquals('x', PersistentDataCoercion.coerce("x", char.class));
        assertArrayEquals(new float[]{1f, 2f}, PersistentDataCoercion.coerce(java.util.List.of(1, 2), float[].class));
    }
}
