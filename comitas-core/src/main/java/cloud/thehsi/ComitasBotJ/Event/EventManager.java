package cloud.thehsi.ComitasBotJ.Event;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.Event;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UndoableEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Utility.StackTraces;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventManager {
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".EventManager");
    private static @NotNull
    final Logger debugLogger = DebugLogging.getLogger();

    private @NotNull
    final Map<Class<? extends Event>, List<RegisteredListener>> listeners = new HashMap<>();

    private @NotNull
    static final ConcurrentHashMap<Long, Long> undoLoopFixCache = new ConcurrentHashMap<>();
    private static final long UNDO_PREVENTION_TTL_MS = 5000;

    public void registerListener(@NotNull Plugin plugin, @NotNull Listener listener) {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Registering Listener {}", listener.getClass());
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class))
                continue;

            EventHandler handler = method.getAnnotation(EventHandler.class);

            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0]))
                continue;

            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass =
                    (Class<? extends Event>) params[0];

            if (DebugLogging.isBasicEnabled())
                debugLogger.debug("Registered Listener Method {}({}) of {}",
                        method.getName(), eventClass.getName(), listener.getClass());

            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle handle = lookup.unreflect(method);

                int slot = handler.priority().getSlot();
                RegisteredListener newListener = new RegisteredListener(slot, listener, plugin, handle);

                List<RegisteredListener> handlerList = listeners.computeIfAbsent(eventClass, k -> new ArrayList<>());

                int insertIndex = 0;
                for (int i = 0; i < handlerList.size(); i++) {
                    if (handlerList.get(i).slot() > slot) {
                        insertIndex = i;
                        break;
                    }
                    insertIndex = i + 1;
                }

                handlerList.add(insertIndex, newListener);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @NotNull
    public static EventOrigin resolveUndoLoopPrevention(@NotNull Class<? extends UndoableEvent> clazz, @Nullable Object... args) {
        long identifier = computeHash(clazz, args);
        Long timestamp = undoLoopFixCache.remove(identifier);

        if (timestamp != null && (System.currentTimeMillis() - timestamp) < UNDO_PREVENTION_TTL_MS) {
            if (DebugLogging.isEventEnabled())
                debugLogger.debug("Ignoring event {}, prevented by UndoLoopFix", clazz.getSimpleName());
            return EventOrigin.UNDO;
        }
        return EventOrigin.EXTERNAL;
    }

    public static void addUndoLoopPrevention(@NotNull Class<? extends UndoableEvent> clazz, @Nullable Object... args) {
        long identifier = computeHash(clazz, args);
        if (DebugLogging.isEventEnabled())
            debugLogger.debug("Adding a UndoLoopFix, as event {} was marked to be undone", clazz.getSimpleName());
        undoLoopFixCache.put(identifier, System.currentTimeMillis());
    }

    private static long computeHash(@NotNull Class<?> clazz, @Nullable Object... args) {
        long hash = clazz.getName().hashCode();
        for (Object arg : args) {
            hash = 31 * hash + (arg == null ? 0 : arg.hashCode());
        }
        return hash;
    }

    public void runCallbackAsync(@NotNull Event event) {
        Thread thread = new Thread(() -> callEvent(event));
        thread.setDaemon(true);
        thread.start();
    }

    public void runCallbackAsync(@NotNull UndoableEvent event,
                                 @NotNull Consumer<UndoableEvent> undoRoutine) {
        Thread thread = new Thread(() -> {
            callEvent(event);
            undoRoutine.accept(event);
        });
        thread.setDaemon(true);
        thread.start();
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
                if (DebugLogging.isEventEnabled()) debugLogger.debug("Calling handler {} for {}", handler.methodHandle(), eventClass.getName());
                handler.methodHandle().invoke(handler.listener(), event);
            } catch (Throwable e) {
                if (!DebugLogging.isBasicEnabled())
                    StackTraces.trimStackTrace(e);

                logger.error(
                        "An error occurred in plugin {}",
                        Objects.requireNonNull(Comitas.getPluginManager().lookupPlugin(handler.plugin)).name(),
                        e
                );
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
            MethodHandle methodHandle
    ) {
    }
}