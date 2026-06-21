package cloud.thehsi.ComitasBotJ.API.Plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Plugin {
    protected Logger getLogger() {
        return LogManager.getLogger(getClass());
    }

    public abstract void onEnable();

    public abstract void onDisable();
}
