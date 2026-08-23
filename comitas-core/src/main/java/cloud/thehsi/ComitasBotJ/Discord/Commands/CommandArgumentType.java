package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRanContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record CommandArgumentType<T>(String name, Class<T> type, OptionType optionType, Function<OptionMapping, T> converter) {
    public @NotNull
    static final CommandArgumentType<CommandRanContext> CONTEXT = new CommandArgumentType<>("CONTEXT", CommandRanContext.class, null, null);

    public @NotNull
    static final CommandArgumentType<String> STRING = new CommandArgumentType<>("STRING", String.class, OptionType.STRING, OptionMapping::getAsString);
    public @NotNull
    static final CommandArgumentType<Integer> INTEGER = new CommandArgumentType<>("INTEGER", Integer.class, OptionType.INTEGER, OptionMapping::getAsInt);
    public @NotNull
    static final CommandArgumentType<Double> DOUBLE = new CommandArgumentType<>("DOUBLE", Double.class, OptionType.NUMBER, OptionMapping::getAsDouble);
    public @NotNull
    static final CommandArgumentType<Boolean> BOOLEAN = new CommandArgumentType<>("BOOLEAN", Boolean.class, OptionType.BOOLEAN, OptionMapping::getAsBoolean);
    public @NotNull
    static final CommandArgumentType<User> USER = new CommandArgumentType<>("USER", User.class, OptionType.USER, m -> new InternalUser(m.getAsUser()));
    public @NotNull
    static final CommandArgumentType<Channel> CHANNEL = new CommandArgumentType<>("CHANNEL", Channel.class, OptionType.CHANNEL, m -> new InternalChannel(m.getAsChannel()));
    public @NotNull
    static final CommandArgumentType<Role> ROLE = new CommandArgumentType<>("ROLE", Role.class, OptionType.ROLE, m -> new InternalRole(m.getAsRole()));
    public @NotNull
    static final CommandArgumentType<Attachment> ATTACHMENT = new CommandArgumentType<>("ATTACHMENT", Attachment.class, OptionType.ATTACHMENT, m -> new InternalAttachment(m.getAsAttachment()));

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> CommandArgumentType<T> fromType(@NotNull Class<T> type) {
        for (CommandArgumentType<?> commandArgumentType : values())
            if (commandArgumentType.type() == type) return (CommandArgumentType<T>) commandArgumentType;
        return null;
    }

    @NotNull
    public static CommandArgumentType<?>[] values() {
        return new CommandArgumentType[]{
                STRING,
                INTEGER,
                DOUBLE,
                BOOLEAN,
                USER,
                CHANNEL,
                ROLE,
                ATTACHMENT
        };
    }
}
