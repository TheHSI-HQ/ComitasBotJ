package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.ThreadTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.Button;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonStyle;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.*;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface UtilityBackend {
    Embed createEmbed(EmbedAuthor author, Color color, Component description, EmbedFooter footer, String image, String thumbnail, EmbedTitle title, TemporalAccessor timestamp, String url, List<EmbedField> fields);

    @Nullable Emoji getEmojiFromId(String id);
    @Nullable Emoji getEmojiFromId(long id);

    Emoji getEmojiFromUnicode(String unicodeEmoji);

    @Nullable User getUserFromId(String id);
    @Nullable User getUserFromId(long id);

    AttachmentUpload uploadAttachment(Path path) throws IOException;
    AttachmentUpload uploadAttachment(String filename, byte[] data);

    ThreadTag createTagOnChannel(Channel channel, String tagName);

    Button createActionButton(String idOrUrl, String label, ButtonStyle buttonStyle, Consumer<ButtonPressedContext> callback);

    Button createActionButton(String idOrUrl, Emoji emoji, ButtonStyle buttonStyle, Consumer<ButtonPressedContext> callback);
}
