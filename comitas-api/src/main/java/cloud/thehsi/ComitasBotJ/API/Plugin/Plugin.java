package cloud.thehsi.ComitasBotJ.API.Plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Plugin {
    default Logger getLogger() {
        return LogManager.getLogger(getClass());
    }

    void onEnable();

    void onDisable();

    String getName();
}
