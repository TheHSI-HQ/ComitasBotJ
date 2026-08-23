package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface Channel {
    /**
     * Returns the channel's Name.
     *
     * @return The channel's Name.
     */
    @NotNull
    String getName();

    /**
     * Returns the channel's Type.
     *
     * @return The channel's Type.
     */
    @NotNull
    ChannelType getType();

    /**
     * Returns the channel's ID.
     *
     * @return The channel's ID
     */
    long getId();

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
    @NotNull
    Component mention();

    /**
     * Get the members of this channel if present
     *
     * @return The channel members if present
     */
    @Nullable
    @Unmodifiable
    List<Member> getMembers();
}
