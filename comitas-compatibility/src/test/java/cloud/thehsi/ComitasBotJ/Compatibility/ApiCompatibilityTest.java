package cloud.thehsi.ComitasBotJ.Compatibility;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.Compatibility.Tools.ApiDiff;
import cloud.thehsi.ComitasBotJ.Compatibility.Tools.ApiSurface;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Compares the current API with the last release (downloaded from Maven Central by the build)
 * and fails, if a breaking change isn't in the changelog.
 */
class ApiCompatibilityTest {
    @Test
    void everyBreakingChangeIsInTheChangelog() throws Exception {
        String version = System.getProperty("comitas.version");
        String previous = System.getProperty("comitas.previous.version");
        String baselineJar = System.getProperty("comitas.baseline.jar");
        assertNotNull(baselineJar, "Run the tests through Maven");
        assertTrue(Files.isRegularFile(Path.of(baselineJar)), "Missing baseline API jar " + baselineJar);

        ApiSurface old = ApiSurface.read(Path.of(baselineJar), ApiChangelog.API_PACKAGE);
        ApiSurface current = ApiSurface.read(currentApi(), ApiChangelog.API_PACKAGE);
        assertFalse(old.classes().isEmpty());
        assertFalse(current.classes().isEmpty());

        List<ApiDiff.Break> uncovered = ApiDiff.uncovered(ApiDiff.breaks(old, current),
                ApiDiff.changesSince(ChangelogRepository.bundled(), previous, version));

        assertTrue(uncovered.isEmpty(), () -> uncovered.size() + " breaking API change(s) since " + previous
                + " are missing in comitas-compatibility/changelog/" + version + ".json:\n"
                + uncovered.stream().map(ApiDiff.Break::displayName).collect(Collectors.joining("\n  ", "  ", "\n"))
                + "\nSuggested entries:\n"
                + uncovered.stream().map(b -> ApiDiff.suggestion(b, current)).collect(Collectors.joining(",\n")));
    }

    @Test
    void detectsRemovedMembers() throws Exception {
        // Sanity check of the diff itself: 0.0.16 -> current has known breaks, which the changelog covers
        String baselineJar = System.getProperty("comitas.baseline.jar");
        assertNotNull(baselineJar, "Run the tests through Maven");
        ApiSurface old = ApiSurface.read(Path.of(baselineJar), ApiChangelog.API_PACKAGE);
        ApiSurface current = ApiSurface.read(currentApi(), ApiChangelog.API_PACKAGE);

        assertTrue(current.hasMember("cloud/thehsi/ComitasBotJ/API/Discord/Channel/TextChannel", "getName()Ljava/lang/String;", false),
                "Inherited members are part of the API");
        if ("0.0.16".equals(System.getProperty("comitas.previous.version")))
            assertTrue(ApiDiff.breaks(old, current).stream().anyMatch(b -> "getDefaultChannel".equals(b.name())));
    }

    private static Path currentApi() throws Exception {
        return Path.of(Guild.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }
}
