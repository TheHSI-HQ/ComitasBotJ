package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface Channel {
    /**
     * Returns the channel's Name.
     *
     * @return The channel's Name.
     */
    String getName();

    /**
     * Returns the channel's Type.
     *
     * @return The channel's Type.
     */
    ChannelType getType();

    /**
     * Returns the channel's ID.
     *
     * @return The channel's ID
     */
    Long getId();

    /**
     * Is this channel NSFW
     *
     * @return The Is this channel nsfw
     */
    boolean isNSFW();

    /**
     * Generates a Mention-String ({@code <#CHANNEÖID>}).
     * <p>
     * Putting this String in any Discord Message, will mention this Channel.
     *
     * @return The generated Mention-Component
     */
    Component mention();

    /**
     * Get the members of this channel if present
     *
     * @return The channel members if present
     */
    @Nullable
    List<Member> getMembers();
}
