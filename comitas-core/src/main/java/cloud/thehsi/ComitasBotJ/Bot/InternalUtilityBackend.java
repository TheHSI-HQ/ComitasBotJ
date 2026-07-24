package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.UtilityBackend;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedAuthor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedFooter;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedTitle;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;

import java.awt.*;
import java.time.temporal.TemporalAccessor;

public class InternalUtilityBackend implements UtilityBackend {
    @Override
    public Embed createEmbed(EmbedAuthor author, Color color, Component description, EmbedFooter footer, String image, String thumbnail, EmbedTitle title, TemporalAccessor timestamp, String url) {
        return new InternalEmbed(
                author,
                color,
                description,
                footer,
                image,
                thumbnail,
                title,
                timestamp,
                url
        );
    }
}
