package cloud.thehsi.ComitasBotJ.API.Event;

import cloud.thehsi.ComitasBotJ.API.Event.Events.Event;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class EventManager {
    private final InternalEventManagerImpl impl;

    public EventManager(InternalEventManagerImpl impl) {
        this.impl = impl;
    }

    public void registerListener(Plugin plugin, Listener listener) {
        impl.registerListener(plugin, listener);
    }

    public void callEvent(Event event) {
        impl.callEvent(event);
    }
}