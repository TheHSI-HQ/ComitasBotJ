package cloud.thehsi.ComitasBotJ.API.Event;

import cloud.thehsi.ComitasBotJ.API.Event.Events.Event;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public interface InternalEventManagerImpl {
    void registerListener(Plugin plugin, Listener listener);

    void callEvent(Event event);
}
