package cloud.thehsi.ComitasBotJ.API.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

import java.util.List;

@SuppressWarnings("unused")
public interface Guild {
    /**
     * Returns the guild's Name.
     *
     * @return The guild's Name.
     */
    String getName();

    /**
     * Returns the guild's ID.
     *
     * @return The guild's ID
     */
    Long getId();

    /**
     * Returns the Default Channel of the guild.
     *
     * @return The default channel of this guild.
     */
    TextChannel getDefaultChannel();

    /**
     * Returns a list of Members of the guild.
     *
     * @return A member list of this guild.
     */
    List<Member> getMembers();

    /**
     * Returns a list of Channels of the guild.
     *
     * @return A channel list of this guild.
     */
    List<Channel> getChannels();
}
