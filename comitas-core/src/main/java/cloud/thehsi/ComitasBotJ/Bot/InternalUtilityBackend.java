package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.UtilityBackend;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.TagNameNotUniqueException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.ThreadTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.*;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Channel.Thread.InternalThreadTag;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachmentUpload;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

public class InternalUtilityBackend implements UtilityBackend {
    @Override
    public Embed createEmbed(EmbedAuthor author, Color color, Component description, EmbedFooter footer, String image, String thumbnail, EmbedTitle title, TemporalAccessor timestamp, String url, List<EmbedField> fields) {
        DebugLogging.action(author, color, description, footer, image, thumbnail, title, timestamp, url, fields);
        return new InternalEmbed(
                author,
                color,
                description,
                footer,
                image,
                thumbnail,
                title,
                timestamp,
                url,
                fields
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

    @Override
    public ThreadTag createTagOnChannel(Channel channel, String tagName) throws TagNameNotUniqueException {
        DebugLogging.action(channel, tagName);

        if (!(channel instanceof InternalChannel internal))
            throw new RuntimeException("Channel was not created by ComitasBotJ");

        if (!(internal.channel instanceof IPostContainer iPostContainer))
            throw new RuntimeException("Channel cannot hold posts");

        List<ForumTagData> tags = new ArrayList<>(iPostContainer.getAvailableTags().stream()
                .map(ForumTagData::from)
                .toList());

        tags.add(new ForumTagData(tagName));

        try {
            iPostContainer.getManager().setAvailableTags(tags).complete();
        } catch (ErrorResponseException e) {
            if (e.getErrorCode() == 40061) // Tag names must be unique
                throw new TagNameNotUniqueException("A tag named '" + tagName + "' already exists.");
        }

        List<ForumTag> found = iPostContainer.getAvailableTagsByName(tagName, false);
        if (found.isEmpty())
            return null;

        return new InternalThreadTag(found.getFirst(), channel.getId());
    }
}
