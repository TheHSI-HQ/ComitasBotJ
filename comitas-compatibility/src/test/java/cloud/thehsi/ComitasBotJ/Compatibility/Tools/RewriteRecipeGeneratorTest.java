package cloud.thehsi.ComitasBotJ.Compatibility.Tools;

import cloud.thehsi.ComitasBotJ.Compatibility.ApiChangelog;
import cloud.thehsi.ComitasBotJ.Compatibility.ChangelogRepository;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RewriteRecipeGeneratorTest {
    @Test
    void generatesChainedRecipes() {
        ApiChangelog first = ApiChangelog.parse(new StringReader("""
                {"schema": 1, "from": "0.0.1", "to": "0.0.2", "changes": [
                  {"id": "a", "kind": "CLASS_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.A",
                   "newName": "cloud.thehsi.ComitasBotJ.API.B", "description": "d"}]}
                """), "first");
        ApiChangelog second = ApiChangelog.parse(new StringReader("""
                {"schema": 1, "from": "0.0.2", "to": "0.0.3", "changes": [
                  {"id": "b", "kind": "METHOD_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.B$Inner", "name": "old",
                   "descriptor": "(I[Ljava/lang/String;Ljava/util/List;)V", "newName": "renamed", "description": "d"},
                  {"id": "c", "kind": "NOTE", "class": "cloud.thehsi.ComitasBotJ.API.B", "name": "x",
                   "description": "Say \\"hi\\""}]}
                """), "second");

        String yaml = RewriteRecipeGenerator.generate(new ChangelogRepository(List.of(first, second)));

        assertTrue(yaml.contains("name: cloud.thehsi.ComitasBotJ.UpgradeTo_0_0_2\n"), yaml);
        assertTrue(yaml.contains("name: cloud.thehsi.ComitasBotJ.UpgradeTo_0_0_3\n"), yaml);
        // Upgrading to 0.0.3 first upgrades to 0.0.2
        assertTrue(yaml.contains("recipeList:\n  - cloud.thehsi.ComitasBotJ.UpgradeTo_0_0_2\n"), yaml);
        assertTrue(yaml.contains("name: cloud.thehsi.ComitasBotJ.UpgradeToLatest\n"), yaml);
        assertTrue(yaml.contains("""
                  - org.openrewrite.java.ChangeType:
                      oldFullyQualifiedTypeName: "cloud.thehsi.ComitasBotJ.API.A"
                      newFullyQualifiedTypeName: "cloud.thehsi.ComitasBotJ.API.B"
                """), yaml);
        assertTrue(yaml.contains("methodPattern: \"cloud.thehsi.ComitasBotJ.API.B$Inner old(int, java.lang.String[], java.util.List)\""), yaml);
        assertTrue(yaml.contains("methodPattern: \"cloud.thehsi.ComitasBotJ.API.B x(..)\""), yaml);
        assertTrue(yaml.contains("newVersion: \"0.0.3\""), yaml);
    }

    @Test
    void bundledChangelogUsesCustomRewrites() {
        String yaml = RewriteRecipeGenerator.generate(ChangelogRepository.bundled());
        assertTrue(yaml.contains("newMethodName: \"getReactorMemberss\""), yaml);
        assertTrue(yaml.contains("methodPattern: \"cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry$ConsoleCommand <constructor>(java.lang.String[], cloud.thehsi.ComitasBotJ.API.Plugin.Plugin, java.lang.String, cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor)\""), yaml);
    }
}
