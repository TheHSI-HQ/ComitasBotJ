package cloud.thehsi.ComitasBotJ.Sandbox;

public class SandboxViolationException extends RuntimeException {

    public SandboxViolationException(String message) {
        super(message);
    }

    public SandboxViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}