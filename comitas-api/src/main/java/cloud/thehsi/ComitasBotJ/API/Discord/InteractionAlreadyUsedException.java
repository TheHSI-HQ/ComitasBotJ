package cloud.thehsi.ComitasBotJ.API.Discord;

public class InteractionAlreadyUsedException extends RuntimeException {
    public InteractionAlreadyUsedException(String message) {
        super(message);
    }
}
