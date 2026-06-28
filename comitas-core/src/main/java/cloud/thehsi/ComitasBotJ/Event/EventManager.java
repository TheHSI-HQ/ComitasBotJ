package cloud.thehsi.ComitasBotJ.Event;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.Events.Event;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".EventManager");

    private final Map<Class<? extends Event>, List<RegisteredListener>> listeners = new HashMap<>();

    public void registerListener(Plugin plugin, Listener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {

            if (!method.isAnnotationPresent(EventHandler.class))
                continue;

            Class<?>[] params = method.getParameterTypes();

            if (params.length != 1)
                continue;

            if (!Event.class.isAssignableFrom(params[0]))
                continue;

            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass =
                    (Class<? extends Event>) params[0];

            listeners
                    .computeIfAbsent(eventClass, k -> new ArrayList<>())
                    .add(new RegisteredListener(listener, plugin, method));
        }
    }

    public void callEvent(Event event) {
        Class<? extends Event> eventClass = null;
        for (Class<?> m : event.getClass().getInterfaces()) {
            if (!Event.class.isAssignableFrom(m)) continue;
            eventClass = m.asSubclass(Event.class);
        }

        if (eventClass == null) throw new RuntimeException("The called Event doesn't implement Event");

        List<RegisteredListener> handlers =
                listeners.get(eventClass);

        if (handlers == null)
            return;

        for (RegisteredListener handler : handlers) {
            try {
                handler.method().invoke(handler.listener(), event);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();

                logger.error(
                        "An error occurred in plugin {}",
                        Comitas.getPluginManager().lookupPlugin(handler.plugin).name(),
                        cause
                );
            } catch (Exception e) {
                logger.error("Failed to invoke listener in plugin {}", Comitas.getPluginManager().lookupPlugin(handler.plugin).name(), e);
            }
        }
    }

    public void clearEvents() {
        listeners.clear();
    }

    private record RegisteredListener(
            Listener listener,
            Plugin plugin,
            Method method
    ) {
    }
}