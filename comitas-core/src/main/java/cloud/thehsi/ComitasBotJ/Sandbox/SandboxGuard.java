package cloud.thehsi.ComitasBotJ.Sandbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SandboxGuard {

    private static final Path SANDBOX_ROOT =
            Paths.get("./plugin_data/dummy").toAbsolutePath().normalize();

    private static final Path REAL_SANDBOX_ROOT;

    static {
        try {
            Files.createDirectories(SANDBOX_ROOT);

            /*
             * Resolve the sandbox root itself so that all later comparisons
             * use the actual filesystem location.
             */
            REAL_SANDBOX_ROOT = SANDBOX_ROOT.toRealPath();

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private SandboxGuard() {
    }

    public static Path rewritePath(Path original) {
        if (original == null) {
            return null;
        }

        return remap(original);
    }

    public static File rewriteFile(File original) {
        if (original == null) {
            return null;
        }

        return remap(original.toPath()).toFile();
    }

    public static String rewriteString(String original) {
        if (original == null) {
            return null;
        }

        return remap(Paths.get(original)).toString();
    }

    /**
     * Maps an arbitrary plugin path into the fake filesystem root.
     * <p>
     * Paths already belonging to the sandbox are retained, but their
     * actual filesystem location is validated.
     */
    private static Path remap(Path original) {
        Path normalized = original.normalize();

        Path absoluteNormalized = normalized.isAbsolute()
                ? normalized
                : normalized.toAbsolutePath().normalize();

        /*
         * Already sandboxed.
         *
         * Do not blindly trust lexical containment because a symlink,
         * junction, or another filesystem redirection could point outside
         * the sandbox.
         */
        if (absoluteNormalized.startsWith(SANDBOX_ROOT)) {
            validateSandboxPath(absoluteNormalized);
            return absoluteNormalized;
        }

        /*
         * Everything outside the sandbox is mapped into the fake root.
         */
        Path relative = normalized.isAbsolute()
                ? normalized.getRoot().relativize(normalized)
                : normalized;

        Path target = SANDBOX_ROOT
                .resolve(relative)
                .normalize();

        if (!target.startsWith(SANDBOX_ROOT)) {
            throw new SandboxViolationException(
                    "Path escaped sandbox: " + original
            );
        }

        validateSandboxPath(target);

        return target;
    }

    /**
     * Ensures that the real filesystem location represented by this path
     * cannot escape the sandbox.
     * <p>
     * The complete target does not necessarily need to exist. For example,
     * this method must work for:
     * <p>
     * Files.createFile(...)
     * <p>
     * Therefore we resolve the nearest existing ancestor and validate that
     * ancestor instead.
     * <p>
     * toRealPath() also resolves filesystem links/reparse points on the
     * platform filesystem provider.
     */
    private static void validateSandboxPath(Path path) {
        Path absolute = path.toAbsolutePath().normalize();

        if (!absolute.startsWith(SANDBOX_ROOT)) {
            throw new SandboxViolationException(
                    "Path escaped sandbox: " + path
            );
        }

        Path existing = absolute;

        try {
            while (!Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) {
                Path parent = existing.getParent();

                if (parent == null || parent.equals(existing)) {
                    break;
                }

                existing = parent;
            }

            Path realExisting = existing.toRealPath();

            if (!realExisting.startsWith(REAL_SANDBOX_ROOT)) {
                throw new SandboxViolationException(
                        "Path escapes sandbox through filesystem link: " + path
                );
            }

        } catch (IOException e) {
            throw new SandboxViolationException(
                    "Unable to validate sandbox path: " + path,
                    e
            );
        }
    }

    public static Path sandboxRoot() {
        return SANDBOX_ROOT;
    }
}