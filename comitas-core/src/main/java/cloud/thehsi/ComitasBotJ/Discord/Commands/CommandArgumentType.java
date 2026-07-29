package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Context;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public record CommandArgumentType<T>(String name, Class<T> type, OptionType optionType, Function<OptionMapping, T> converter) {
    public static final CommandArgumentType<Context> CONTEXT = new CommandArgumentType<>("CONTEXT", Context.class, null, null);

    public static final CommandArgumentType<String> STRING = new CommandArgumentType<>("STRING", String.class, OptionType.STRING, OptionMapping::getAsString);
    public static final CommandArgumentType<Integer> INTEGER = new CommandArgumentType<>("INTEGER", Integer.class, OptionType.INTEGER, OptionMapping::getAsInt);
    public static final CommandArgumentType<Double> DOUBLE = new CommandArgumentType<>("DOUBLE", Double.class, OptionType.NUMBER, OptionMapping::getAsDouble);
    public static final CommandArgumentType<Boolean> BOOLEAN = new CommandArgumentType<>("BOOLEAN", Boolean.class, OptionType.BOOLEAN, OptionMapping::getAsBoolean);
    public static final CommandArgumentType<Member> MEMBER = new CommandArgumentType<>("MEMBER", Member.class, OptionType.USER, m -> new InternalMember(Objects.requireNonNull(m.getAsMember())));
    public static final CommandArgumentType<Channel> CHANNEL = new CommandArgumentType<>("CHANNEL", Channel.class, OptionType.CHANNEL, m -> new InternalChannel(m.getAsChannel()));
    public static final CommandArgumentType<Role> ROLE = new CommandArgumentType<>("ROLE", Role.class, OptionType.ROLE, m -> new InternalRole(m.getAsRole()));
    public static final CommandArgumentType<Attachment> ATTACHMENT = new CommandArgumentType<>("ATTACHMENT", Attachment.class, OptionType.ATTACHMENT, m -> new InternalAttachment(m.getAsAttachment()));

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> CommandArgumentType<T> fromType(Class<T> type) {
        for (CommandArgumentType<?> commandArgumentType : values())
            if (commandArgumentType.type() == type) return (CommandArgumentType<T>) commandArgumentType;
        return null;
    }

    public static CommandArgumentType<?>[] values() {
        return new CommandArgumentType[]{
                STRING,
                INTEGER,
                DOUBLE,
                BOOLEAN,
                MEMBER,
                CHANNEL,
                ROLE,
                ATTACHMENT
        };
    }
}
