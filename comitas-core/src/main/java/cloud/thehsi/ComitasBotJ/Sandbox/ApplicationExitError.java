package cloud.thehsi.ComitasBotJ.Sandbox;

/**
 * Thrown by {@code System.exit} / {@code Runtime.exit} / {@code Runtime.halt} in the code of a
 * {@link SandboxJavaApplication}, it ends the application's main method instead of the server.
 * <p>
 * An {@link Error}, so that the usual {@code catch (Exception e)} of an application doesn't swallow it.
 */
final class ApplicationExitError extends Error {
    private final int status;

    ApplicationExitError(int status) {
        super("System.exit(" + status + ") of a plugin application", null, false, false);
        this.status = status;
    }

    int status() {
        return status;
    }
}
