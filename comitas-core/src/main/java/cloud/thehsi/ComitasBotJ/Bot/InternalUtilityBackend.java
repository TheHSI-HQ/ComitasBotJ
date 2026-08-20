package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.UtilityBackend;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedAuthor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedFooter;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedTitle;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachmentUpload;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;

public class InternalUtilityBackend implements UtilityBackend {
    @Override
    public Embed createEmbed(EmbedAuthor author, Color color, Component description, EmbedFooter footer, String image, String thumbnail, EmbedTitle title, TemporalAccessor timestamp, String url) {
        DebugLogging.action(author, color, description, footer, image, thumbnail, title, timestamp, url);
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

    @Override
    @Nullable
    public Emoji getEmojiFromId(String id) {
        DebugLogging.action(id);
        RichCustomEmoji customEmoji = DiscordAPI.api().getEmojiById(id);
        if (customEmoji == null) return null;
        return new InternalEmoji(customEmoji);
    }

    @Override
    @Nullable
    public Emoji getEmojiFromId(long id) {
        DebugLogging.action(id);
        RichCustomEmoji customEmoji = DiscordAPI.api().getEmojiById(id);
        if (customEmoji == null) return null;
        return new InternalEmoji(customEmoji);
    }

    @Override
    @Nullable
    public Emoji getEmojiFromUnicode(String unicodeEmoji) {
        DebugLogging.action(unicodeEmoji);
        return new InternalEmoji(unicodeEmoji);
    }

    @Override
    public @Nullable User getUserFromId(String id) {
        DebugLogging.action(id);
        return new InternalUser(DiscordAPI.api().getUserById(id));
    }

    @Override
    public @Nullable User getUserFromId(long id) {
        DebugLogging.action(id);
        return new InternalUser(DiscordAPI.api().getUserById(id));
    }

    @Override
    public AttachmentUpload uploadAttachment(Path path) throws IOException {
        DebugLogging.action(path);
        return new InternalAttachmentUpload(path);
    }

    @Override
    public AttachmentUpload uploadAttachment(String filename, byte[] data) {
        DebugLogging.action(filename, data);
        return new InternalAttachmentUpload(filename, data);
    }
}
