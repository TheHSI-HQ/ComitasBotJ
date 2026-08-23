package cloud.thehsi.ComitasBotJ.Event;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.Events.Event;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EventManager {
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".EventManager");
    private @NotNull
    final Logger debugLogger = DebugLogging.getLogger();

    private @NotNull
    final Map<Class<? extends Event>, List<RegisteredListener>> listeners = new HashMap<>();

    public void registerListener(@NotNull Plugin plugin, @NotNull Listener listener) {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Registering Listener {}", listener.getClass());
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class))
                continue;

            EventHandler handler = method.getAnnotation(EventHandler.class);

            Class<?>[] params = method.getParameterTypes();

            if (params.length != 1)
                continue;

            if (!Event.class.isAssignableFrom(params[0]))
                continue;

            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass =
                    (Class<? extends Event>) params[0];

            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Resgistering Listener Method {}({}) of {}", method.getName(), eventClass.getName(), listener.getClass());
            listeners
                    .computeIfAbsent(eventClass, k -> new ArrayList<>())
                    .add(new RegisteredListener(handler.priority().getSlot(), listener, plugin, method));

            listeners.get(eventClass)
                    .sort(Comparator.comparingInt(RegisteredListener::slot)); // Sort by Slot
        }
    }

    public void callEvent(@NotNull Event event) {
        Class<? extends Event> eventClass = null;
        for (Class<?> m : event.getClass().getInterfaces()) {
            if (!Event.class.isAssignableFrom(m)) continue;
            eventClass = m.asSubclass(Event.class);
        }

        if (eventClass == null) throw new RuntimeException("The called Event doesn't implement Event");
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Calling event {}", eventClass.getName());

        List<RegisteredListener> handlers =
                listeners.get(eventClass);

        if (handlers == null)
            return;

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Found {} handlers for {}", handlers.size(), eventClass.getName());

        for (RegisteredListener handler : handlers) {
            try {
                if (DebugLogging.isEventEnabled()) debugLogger.debug("Calling handler {} for {}", handler.method(), eventClass.getName());
                handler.method().invoke(handler.listener(), event);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();

                logger.error(
                        "An error occurred in plugin {}",
                        Objects.requireNonNull(Comitas.getPluginManager().lookupPlugin(handler.plugin)).name(),
                        cause
                );
            } catch (Exception e) {
                logger.error("Failed to invoke listener in plugin {}", Objects.requireNonNull(Comitas.getPluginManager().lookupPlugin(handler.plugin)).name(), e);
            }
        }
    }

    public void clearEventListeners() {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Clearing {} event listeners", listeners.size());
        listeners.clear();
    }

    private record RegisteredListener(
            int slot,
            Listener listener,
            Plugin plugin,
            Method method
    ) {
    }
}