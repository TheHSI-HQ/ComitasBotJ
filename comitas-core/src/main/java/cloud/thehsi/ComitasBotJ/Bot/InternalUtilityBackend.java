package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.UtilityBackend;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.TagNameNotUniqueException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.ThreadTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.Button;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonStyle;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.*;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Channel.Thread.InternalThreadTag;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import cloud.thehsi.ComitasBotJ.Discord.Message.Actions.InternalButton;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachmentUpload;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InternalUtilityBackend implements UtilityBackend {
    @Override
    public @NotNull Embed createEmbed(@Nullable EmbedAuthor author, @Nullable Color color, @Nullable Component description, @Nullable EmbedFooter footer, @Nullable String image, @Nullable String thumbnail, @Nullable EmbedTitle title, @Nullable TemporalAccessor timestamp, @Nullable String url, @NotNull List<EmbedField> fields) {
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
    public Emoji getEmojiFromId(@NotNull String id) {
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
    public @NotNull Emoji getEmojiFromUnicode(@NotNull String unicodeEmoji) {
        DebugLogging.action(unicodeEmoji);
        return new InternalEmoji(unicodeEmoji);
    }

    @Override
    public @Nullable User getUserFromId(@NotNull String id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.User user = DiscordAPI.api().getUserById(id);
        return user == null ?
                null : new InternalUser(user);
    }

    @Override
    public @Nullable User getUserFromId(long id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.User user = DiscordAPI.api().getUserById(id);
        return user == null ?
                null : new InternalUser(user);
    }

    @Override
    public @NotNull AttachmentUpload uploadAttachment(@NotNull Path path) throws IOException {
        DebugLogging.action(path);
        return new InternalAttachmentUpload(path);
    }

    @Override
    public @NotNull AttachmentUpload uploadAttachment(@NotNull String filename, byte[] data) {
        DebugLogging.action(filename, data);
        return new InternalAttachmentUpload(filename, data);
    }

    @Override
    public @NotNull ThreadTag createTagOnChannel(@NotNull Channel channel, @NotNull String tagName) throws TagNameNotUniqueException {
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
            throw new RuntimeException("Couldn't locate newly created tag. Maybe missing permissions?"); // TODO: Replace with custom exception

        return new InternalThreadTag(found.getFirst(), channel.getId());
    }

    @Override
    public @NotNull Button createActionButton(@NotNull String idOrUrl, @NotNull String label, @NotNull ButtonStyle buttonStyle, @Nullable Consumer<ButtonPressedContext> callback) {
        DebugLogging.action(idOrUrl, label, buttonStyle, callback);
        return new InternalButton(idOrUrl, label, buttonStyle, callback);
    }

    @Override
    public @NotNull Button createActionButton(@NotNull String idOrUrl, @NotNull Emoji emoji, @NotNull ButtonStyle buttonStyle, @Nullable Consumer<ButtonPressedContext> callback) {
        DebugLogging.action(idOrUrl, emoji, buttonStyle, callback);
        return new InternalButton(idOrUrl, emoji, buttonStyle, callback);
    }
}
