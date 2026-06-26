package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

@SuppressWarnings("unused")
public interface Channel {
    /**
     * Returns the channel's Name.
     *
     * @return The channel's Name.
     */
    String getName();

    /**
     * Returns the channel's ID.
     *
     * @return The channel's ID
     */
    Long getId();

    /**
     * Generates a Mention-String ({@code <#CHANNEÖID>}).
     * <p>
     * Putting this String in any Discord Message, will mention this Channel.
     *
     * @return The generated Mention-String
     */
    String mention();
}
