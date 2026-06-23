package cloud.thehsi.ComitasBotJ.API.Plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public abstract class Plugin {
    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    public abstract void onEnable();

    public abstract void onDisable();
}
