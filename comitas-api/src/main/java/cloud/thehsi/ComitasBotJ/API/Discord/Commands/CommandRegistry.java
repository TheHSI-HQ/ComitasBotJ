package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface CommandRegistry {
    /**
     * Register a command
     *
     * @param command The CommandSupplier with the commands
     */

    void register(@NotNull CommandSupplier command);
}
