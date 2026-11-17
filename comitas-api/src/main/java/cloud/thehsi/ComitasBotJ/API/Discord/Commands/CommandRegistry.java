package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandParentBuilder;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.DynamicCommandBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * The command registry is used to register commands, either dynamically
 * or using the prefered static {@link CommandSupplier} with a {@link Command}
 * <p>
 * Subcommands are registered the same way, but their parent command has to be declared first, either with a
 * {@link CommandParent} on the {@link CommandSupplier}, or using {@link #register(CommandParentBuilder)}
 */
@SuppressWarnings("unused")
public interface CommandRegistry {
    /**
     * Register a command
     * <p>
     * Any {@link CommandParent} on the suppliers class is declared before its commands get registered, so
     * a supplier can hold a parent command together with its subcommands
     *
     * @param command The CommandSupplier with the commands
     * @throws IllegalStateException    If a subcommand references a parent (or group), which wasn't declared
     * @throws IllegalArgumentException If a command or subcommand with the same name is already registered
     */
    void register(@NotNull CommandSupplier command);

    /**
     * Register a command using a dynamic command builder.
     * This method provides less type saftey but allows for creating commands
     * at runtime instead of at build time
     *
     * @param commandBuilder The finished DynamicCommandBuilder
     * @throws IllegalStateException    If it's a subcommand and the parent (or group) wasn't declared
     * @throws IllegalArgumentException If a command or subcommand with the same name is already registered
     */
    void register(@NotNull DynamicCommandBuilder commandBuilder);

    /**
     * Declare a parent command at runtime, which holds subcommands.
     * This is the dynamic counterpart of {@link CommandParent}, register it before its subcommands.
     * <p>
     * Declaring the same parent again is fine, the first declaration wins, but additional groups are merged in.
     * The command only shows up on Discord once it has at least one subcommand.
     *
     * @param parentBuilder The finished CommandParentBuilder
     * @throws IllegalArgumentException If a regular command with the same name is already registered
     */
    void register(@NotNull CommandParentBuilder parentBuilder);
}
