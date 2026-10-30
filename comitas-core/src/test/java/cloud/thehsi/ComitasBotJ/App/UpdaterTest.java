package cloud.thehsi.ComitasBotJ.App;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UpdaterTest {
    private static JsonObject release(String assets) {
        return JsonParser.parseString("{\"tag_name\": \"v1.2.3\", \"assets\": [" + assets + "]}").getAsJsonObject();
    }

    @Test
    void parsesTheCoreJarAndItsDigest() throws IOException {
        Updater.Release release = Updater.parseRelease(release("""
                {"name": "comitas-api-1.2.3.jar", "browser_download_url": "https://example.com/api.jar", "digest": "sha256:00"},
                {"name": "comitas-core-1.2.3.jar", "browser_download_url": "https://example.com/core.jar", "digest": "sha256:ABCDEF"}
                """));

        assertEquals("v1.2.3", release.version());
        assertEquals("https://example.com/core.jar", release.download().toString());
        assertEquals("abcdef", release.sha256());
    }

    @Test
    void refusesReleasesWithoutDigest() {
        assertThrows(IOException.class, () -> Updater.parseRelease(release("""
                {"name": "comitas-core-1.2.3.jar", "browser_download_url": "https://example.com/core.jar", "digest": null}
                """)));
    }

    @Test
    void refusesReleasesWithoutCoreJar() {
        assertThrows(IOException.class, () -> Updater.parseRelease(release("")));
    }

    @Test
    void hashesWithSha256() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                Updater.sha256("abc".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void refusesFilesThatArentCoreJars(@TempDir Path temp) throws IOException {
        Path file = Files.writeString(temp.resolve("server.jar.update"), "<html>Not found</html>");
        assertThrows(IOException.class, () -> Updater.verifyJar(file));
    }
}
