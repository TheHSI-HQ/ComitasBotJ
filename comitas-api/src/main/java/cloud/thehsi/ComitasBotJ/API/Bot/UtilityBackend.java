package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedAuthor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedFooter;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedTitle;

import java.awt.*;
import java.time.temporal.TemporalAccessor;

@SuppressWarnings("unused")
public interface UtilityBackend {
    Embed createEmbed(EmbedAuthor author, Color color, Component description, EmbedFooter footer, String image, String thumbnail, EmbedTitle title, TemporalAccessor timestamp, String url);
}
