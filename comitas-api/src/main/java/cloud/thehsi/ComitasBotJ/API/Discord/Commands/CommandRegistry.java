package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.DynamicCommandBuilder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface CommandRegistry {
    /**
     * Register a command
     *
     * @param command The CommandSupplier with the commands
     */
    void register(@NotNull CommandSupplier command);

    /**
     * Register a command using a dynamic command builder.
     * This method provides less type saftey but allows for creating commands
     * at runtime instead of at build time
     *
     * @param commandBuilder The finished DynamicCommandBuilder
     */
    void register(@NotNull DynamicCommandBuilder commandBuilder);
}
