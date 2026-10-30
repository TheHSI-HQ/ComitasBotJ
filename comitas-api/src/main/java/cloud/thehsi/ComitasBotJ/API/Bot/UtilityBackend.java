package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.Button;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonStyle;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.*;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.function.Consumer;

/**
 * The utility backend is an internal communication way for
 * the api and the comitas backend
 *
 * @hidden
 */
@SuppressWarnings({"unused", "MissingJavadoc"})
@ApiStatus.Internal
public interface UtilityBackend {
    @NotNull
    @ApiStatus.Internal
    Embed createEmbed(@Nullable EmbedAuthor author,
                               @Nullable Color color,
                               @Nullable Component description,
                               @Nullable EmbedFooter footer,
                               @Nullable String image,
                               @Nullable String thumbnail,
                               @Nullable EmbedTitle title,
                               @Nullable TemporalAccessor timestamp,
                               @Nullable String url,
                               @NotNull List<EmbedField> fields);

    @Nullable
    @ApiStatus.Internal
    Emoji getEmojiFromId(@NotNull String id);

    @Nullable
    @ApiStatus.Internal
    Emoji getEmojiFromId(long id);

    @Nullable
    @ApiStatus.Internal
    Emoji getEmojiFromUnicode(@NotNull String unicodeEmoji);

    @Nullable
    @ApiStatus.Internal
    User getUserFromId(@NotNull String id);

    @Nullable
    @ApiStatus.Internal
    User getUserFromId(long id);

    @NotNull
    @ApiStatus.Internal
    AttachmentUpload uploadAttachment(@NotNull Path path) throws IOException;

    @NotNull
    @ApiStatus.Internal
    AttachmentUpload uploadAttachment(@NotNull String filename, byte[] data);

    @NotNull
    @ApiStatus.Internal
    AttachmentUpload uploadVoiceMessage(@NotNull Path path) throws IOException;

    @NotNull
    @ApiStatus.Internal
    AttachmentUpload uploadVoiceMessage(@NotNull String filename, byte[] data);

    @NotNull
    @ApiStatus.Internal
    ForumTag createTagOnChannel(@NotNull Channel channel, @NotNull String tagName);

    @NotNull
    @ApiStatus.Internal
    Button createActionButton(@NotNull String idOrUrl, @NotNull String label, @NotNull ButtonStyle buttonStyle, @Nullable Consumer<ButtonPressedContext> callback);

    @NotNull
    @ApiStatus.Internal
    Button createActionButton(@NotNull String idOrUrl, @NotNull Emoji emoji, @NotNull ButtonStyle buttonStyle, @Nullable Consumer<ButtonPressedContext> callback);
}
