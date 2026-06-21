package cloud.thehsi.ComitasBotJ.API.Plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Plugin {
    Logger logger =
            LogManager.getLogger();

    void onEnable();
    void onDisable();

    String getName();
}
