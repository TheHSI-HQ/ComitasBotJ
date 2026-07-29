package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

@SuppressWarnings("unused")
public interface CommandRegistry {
    void register(CommandSupplier command);
}
