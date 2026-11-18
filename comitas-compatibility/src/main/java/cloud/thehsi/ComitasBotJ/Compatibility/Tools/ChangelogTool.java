package cloud.thehsi.ComitasBotJ.Compatibility.Tools;

import cloud.thehsi.ComitasBotJ.Compatibility.ApiChangelog;
import cloud.thehsi.ComitasBotJ.Compatibility.Change;
import cloud.thehsi.ComitasBotJ.Compatibility.ChangeKind;
import cloud.thehsi.ComitasBotJ.Compatibility.ChangelogRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Command line tools for the API changelog.
 * <pre>
 * rewrite &lt;out.yml&gt;                        Generates the OpenRewrite upgrade recipes
 * markdown [out.md]                         Writes a human readable changelog
 * diff &lt;old-api.jar&gt; &lt;new-api&gt; &lt;old&gt; &lt;new&gt;  Lists breaking changes missing in the changelog
 * </pre>
 */
public final class ChangelogTool {
    private ChangelogTool() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            usage();
            return;
        }

        ChangelogRepository repository = ChangelogRepository.bundled();
        switch (args[0]) {
            case "rewrite" -> {
                if (args.length != 2) {
                    usage();
                    return;
                }
                write(Path.of(args[1]), RewriteRecipeGenerator.generate(repository));
            }
            case "markdown" -> {
                String markdown = markdown(repository);
                if (args.length > 1) write(Path.of(args[1]), markdown);
                else System.out.print(markdown);
            }
            case "diff" -> {
                if (args.length != 5) {
                    usage();
                    return;
                }
                ApiSurface old = ApiSurface.read(Path.of(args[1]), ApiChangelog.API_PACKAGE);
                ApiSurface current = ApiSurface.read(Path.of(args[2]), ApiChangelog.API_PACKAGE);
                List<ApiDiff.Break> uncovered = ApiDiff.uncovered(ApiDiff.breaks(old, current),
                        ApiDiff.changesSince(repository, args[3], args[4]));
                if (uncovered.isEmpty()) {
                    System.out.println("Every breaking change since " + args[3] + " is in the changelog");
                    return;
                }
                System.out.println(uncovered.size() + " breaking change(s) since " + args[3] + " are missing in the changelog:");
                for (ApiDiff.Break b : uncovered) System.out.println(ApiDiff.suggestion(b, current));
                System.exit(1);
            }
            default -> usage();
        }
    }

    private static void usage() {
        System.err.println("""
                Usage:
                  rewrite <out.yml>                          Generates the OpenRewrite upgrade recipes
                  markdown [out.md]                          Writes a human readable changelog
                  diff <old-api.jar> <new-api> <old> <new>   Lists breaking changes missing in the changelog""");
    }

    private static void write(@NotNull Path path, @NotNull String content) throws IOException {
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    @NotNull
    static String markdown(@NotNull ChangelogRepository repository) {
        StringBuilder md = new StringBuilder("# Comitas API Changes\n");
        List<ApiChangelog> changelogs = repository.changelogs().reversed();
        for (ApiChangelog changelog : changelogs) {
            md.append("\n## ").append(changelog.to()).append(" (from ").append(changelog.from()).append(")\n\n");
            if (changelog.changes().isEmpty()) {
                md.append("No breaking changes.\n");
                continue;
            }
            md.append("| Change | Affects | Old plugins | Migration |\n");
            md.append("|--------|---------|-------------|-----------|\n");
            for (Change change : changelog.changes()) {
                md.append("| ").append(cell(change.description()))
                        .append(" | `").append(cell(change.displayName())).append('`')
                        .append(" | ").append(fix(change.kind().fix()))
                        .append(" | ").append(cell(change.migration() == null ? "Automatic" : change.migration()))
                        .append(" |\n");
            }
        }
        return md.toString();
    }

    @NotNull
    private static String fix(@NotNull ChangeKind.Fix fix) {
        return switch (fix) {
            case AUTOMATIC -> "Fixed automatically";
            case SHIM -> "Emulated";
            case MANUAL -> "May misbehave";
        };
    }

    @NotNull
    private static String cell(@NotNull String text) {
        return text.replace("|", "\\|").replace("\n", " ");
    }
}
