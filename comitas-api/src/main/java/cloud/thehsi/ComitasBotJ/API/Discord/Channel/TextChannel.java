package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

@SuppressWarnings("unused")
public interface TextChannel extends Channel {
    /**
     * Send a Message in the Channel
     *
     * @param message The message to be sent
     */
    void sendMessage(String message);
}
