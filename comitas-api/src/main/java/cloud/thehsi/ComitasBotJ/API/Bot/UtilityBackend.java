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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface UtilityBackend {
    @NotNull Embed createEmbed(@Nullable EmbedAuthor author,
                               @Nullable Color color,
                               @Nullable Component description,
                               @Nullable EmbedFooter footer,
                               @Nullable String image,
                               @Nullable String thumbnail,
                               @Nullable EmbedTitle title,
                               @Nullable TemporalAccessor timestamp,
                               @Nullable String url,
                               @NotNull List<EmbedField> fields);

    @Nullable Emoji getEmojiFromId(@NotNull String id);
    @Nullable Emoji getEmojiFromId(long id);

    @Nullable Emoji getEmojiFromUnicode(@NotNull String unicodeEmoji);

    @Nullable User getUserFromId(@NotNull String id);
    @Nullable User getUserFromId(long id);

    @NotNull AttachmentUpload uploadAttachment(@NotNull Path path) throws IOException;

    @NotNull AttachmentUpload uploadAttachment(@NotNull String filename, byte[] data);

    @NotNull ForumTag createTagOnChannel(@NotNull Channel channel, @NotNull String tagName);

    @NotNull Button createActionButton(@NotNull String idOrUrl, @NotNull String label, @NotNull ButtonStyle buttonStyle, @Nullable Consumer<ButtonPressedContext> callback);

    @NotNull Button createActionButton(@NotNull String idOrUrl, @NotNull Emoji emoji, @NotNull ButtonStyle buttonStyle, @Nullable Consumer<ButtonPressedContext> callback);
}
