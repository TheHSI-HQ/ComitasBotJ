package cloud.thehsi.ComitasBotJ.API.Event.Events;

public class BotConnectEvent extends Event {
    private final String username;

    public BotConnectEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}