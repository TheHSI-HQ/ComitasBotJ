package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ChannelType;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.VoiceChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelTypeTest {
    @Test
    void voiceAndStageChannelsAreVoiceChannels() {
        assertEquals(VoiceChannel.class, ChannelType.VOICE.clazz());
        assertEquals(VoiceChannel.class, ChannelType.STAGE.clazz());
        assertTrue(ChannelType.VOICE.isAudio());
        assertTrue(ChannelType.STAGE.isMessage());
    }
}
