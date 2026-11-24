package cloud.thehsi.ComitasBotJ.Sandbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OutstandingRequestsTest {
    private static final UUID PLUGIN = UUID.fromString("38123123-1312-4321-8123-123123132130");

    @TempDir
    Path temp;

    private static OutstandingRequests.Entry permission(String key, boolean risky) {
        return new OutstandingRequests.Entry(OutstandingRequests.Kind.PERMISSION, "permission." + key, key,
                "Needs " + key, false, risky, null, "?", null);
    }

    private Path file() {
        return temp.resolve("outstanding-requests.txt");
    }

    /**
     * Writes a file with the given requests, as the first start does.
     */
    private String firstStart(OutstandingRequests.Entry... entries) throws IOException {
        OutstandingRequests requests = OutstandingRequests.load(file());
        requests.plugin(PLUGIN, "Weather", "1.2.0", "weather.jar");
        for (OutstandingRequests.Entry entry : entries) requests.add(PLUGIN, entry);
        assertEquals(entries.length, requests.finishStartup());
        return Files.readString(file());
    }

    @Test
    void unansweredRequestsAreListed() throws IOException {
        String content = firstStart(permission("network", false), permission("risky_libraries", true));
        assertTrue(content.contains("Weather 1.2.0"));
        assertTrue(content.contains("[" + PLUGIN + "]"));
        assertTrue(content.contains("permission.network=?"));
        assertTrue(content.contains("Reason: Needs network"));
        assertTrue(content.contains("RISKY Permission: risky_libraries"));
        assertTrue(content.strip().endsWith("ALLOW_ALL=N"));
        assertTrue(content.indexOf("WARNING") < content.lastIndexOf("ALLOW_ALL=N"));
    }

    @Test
    void answersAreReadAndTheFileIsDeletedOnceEverythingIsAnswered() throws IOException {
        firstStart(permission("network", false), permission("file_storage", false));
        Files.writeString(file(), Files.readString(file())
                .replace("permission.network=?", "permission.network=Y")
                .replace("permission.file_storage=?", "permission.file_storage= no "));

        OutstandingRequests second = OutstandingRequests.load(file());
        assertEquals(Boolean.TRUE, second.decision(PLUGIN, "permission.network", false));
        assertEquals(Boolean.FALSE, second.decision(PLUGIN, "permission.file_storage", false));
        assertNull(second.decision(PLUGIN, "permission.server_control", false));
        second.resolved(PLUGIN, "permission.network");
        second.resolved(PLUGIN, "permission.file_storage");
        assertEquals(0, second.finishStartup());
        assertFalse(Files.exists(file()));
    }

    @Test
    void allowAllOnlyGrantsListedRequestsWhichArentRisky() throws IOException {
        firstStart(permission("network", false), permission("risky_libraries", true));
        Files.writeString(file(), Files.readString(file()).replace("ALLOW_ALL=N", "ALLOW_ALL=Y"));

        OutstandingRequests second = OutstandingRequests.load(file());
        assertEquals(Boolean.TRUE, second.decision(PLUGIN, "permission.network", false));
        assertNull(second.decision(PLUGIN, "permission.risky_libraries", true));
        // Requested after the owner set ALLOW_ALL, they never saw it
        assertNull(second.decision(PLUGIN, "permission.server_control", false));
        assertNull(second.decision(UUID.randomUUID(), "permission.network", false));

        second.resolved(PLUGIN, "permission.network");
        second.add(PLUGIN, permission("risky_libraries", true));
        assertEquals(1, second.finishStartup());
        String content = Files.readString(file());
        assertTrue(content.strip().endsWith("ALLOW_ALL=N"));
        assertFalse(content.contains("permission.network"));
    }

    @Test
    void invalidAnswersStayWithAHint() throws IOException {
        firstStart(permission("network", false));
        Files.writeString(file(), Files.readString(file()).replace("permission.network=?", "permission.network=maybe"));

        OutstandingRequests second = OutstandingRequests.load(file());
        assertNull(second.decision(PLUGIN, "permission.network", false));
        second.add(PLUGIN, permission("network", false));
        second.finishStartup();
        String content = Files.readString(file());
        assertTrue(content.contains("permission.network=maybe"));
        assertTrue(content.contains("Invalid answer 'maybe'"));
    }

    @Test
    void pluginTextCantAddAnswers() throws IOException {
        OutstandingRequests requests = OutstandingRequests.load(file());
        requests.plugin(PLUGIN, "Evil\nALLOW_ALL=Y\n[" + PLUGIN + "]", "1.0\rpermission.risky_libraries=Y", "evil.jar");
        requests.add(PLUGIN, new OutstandingRequests.Entry(OutstandingRequests.Kind.PERMISSION, "permission.network",
                "Network", "line\npermission.risky_libraries=Y ALLOW_ALL=Y", false, false, null, "?", null));
        requests.finishStartup();

        OutstandingRequests reread = OutstandingRequests.load(file());
        assertNull(reread.decision(PLUGIN, "permission.risky_libraries", false));
        assertFalse(reread.allowAll(PLUGIN, "permission.network"));
        for (String line : Files.readAllLines(file()))
            assertTrue(line.isBlank() || line.startsWith("#") || line.startsWith("[" + PLUGIN)
                    || line.equals("permission.network=?") || line.equals("ALLOW_ALL=N"), line);
    }
}
