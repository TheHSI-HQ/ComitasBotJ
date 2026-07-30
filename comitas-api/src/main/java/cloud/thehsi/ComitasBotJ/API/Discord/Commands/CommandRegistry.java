package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

@SuppressWarnings("unused")
public interface CommandRegistry {
    /**
     * Register a command
     *
     * @param command The CommandSupplier with the commands
     */

    void register(CommandSupplier command);
}
