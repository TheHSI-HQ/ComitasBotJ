package cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a command arguments type
 *
 * @param name The type name
 * @param type The raw type class
 * @param <T>  The raw type
 */
public record CommandArgumentType<T>(String name, Class<T> type) {
    /**
     * The String Command Argument Type
     */
    public @NotNull
    static final CommandArgumentType<String> STRING = new CommandArgumentType<>("STRING", String.class);
    /**
     * The Integer Command Argument Type
     */
    public @NotNull
    static final CommandArgumentType<Integer> INTEGER = new CommandArgumentType<>("INTEGER", Integer.class);
    /**
     * The Double Command Argument Type
     */
    public @NotNull
    static final CommandArgumentType<Double> DOUBLE = new CommandArgumentType<>("DOUBLE", Double.class);
    /**
     * The Booelan Command Argument Type
     */
    public @NotNull
    static final CommandArgumentType<Boolean> BOOLEAN = new CommandArgumentType<>("BOOLEAN", Boolean.class);
    /**
     * The User Command Argument Type
     */
    public @NotNull
    static final CommandArgumentType<User> USER = new CommandArgumentType<>("USER", User.class);
    /**
     * The Channel Command Argument Type
     */
    public @NotNull
    static final CommandArgumentType<Channel> CHANNEL = new CommandArgumentType<>("CHANNEL", Channel.class);
    /**
     * The Role Command Argument Type
     */
    public @NotNull
    static final CommandArgumentType<Role> ROLE = new CommandArgumentType<>("ROLE", Role.class);
    /**
     * The Attachment Command Argument Type
     */
    public @NotNull
    static final CommandArgumentType<Attachment> ATTACHMENT = new CommandArgumentType<>("ATTACHMENT", Attachment.class);

    /**
     * Cast an object into this type
     *
     * @param o The object to cast
     * @return The cast object if castable, else null
     */
    public @Nullable T cast(@Nullable Object o) {
        if (!type.isInstance(o))
            return null;

        return type.cast(o);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CommandArgumentType<?> that = (CommandArgumentType<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
