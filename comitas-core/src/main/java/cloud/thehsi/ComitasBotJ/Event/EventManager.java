package cloud.thehsi.ComitasBotJ.Event;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.Event;
import cloud.thehsi.ComitasBotJ.API.Event.Events.PackageSentEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UndoableEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import cloud.thehsi.ComitasBotJ.Utility.StackTraces;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class EventManager {
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".EventManager");
    private static @NotNull
    final Logger debugLogger = DebugLogging.getLogger();

    private @NotNull
    final Map<Class<? extends Event>, List<RegisteredListener>> listeners = new ConcurrentHashMap<>();

    private @NotNull
    static final ConcurrentHashMap<Long, Long> undoLoopFixCache = new ConcurrentHashMap<>();
    private static final long UNDO_PREVENTION_TTL_MS = 5000;

    private @NotNull
    static final ExecutorService asyncEventExecutor =
            Executors.newFixedThreadPool(8, r -> {
                Thread t = new Thread(r, "ComitasEventWorker");
                t.setDaemon(true);
                return t;
            });

    /**
     * Extracts the concrete generic type argument of a single-parameter event
     * handler method (e.g. the {@code TestPackage} in
     * {@code PackageSentEvent<TestPackage>}), so dispatch can later filter by it.
     * <p>
     * Returns {@code null} when the parameter isn't parameterized, or its type
     * argument isn't a concrete class (e.g. a wildcard or type variable), in
     * which case no filtering is applied and the handler receives every event.
     */
    @Nullable
    private static Class<?> extractGenericFilter(@NotNull Method method) {
        Type[] genericParams = method.getGenericParameterTypes();
        if (genericParams.length != 1 || !(genericParams[0] instanceof ParameterizedType parameterizedType))
            return null;

        Type[] typeArgs = parameterizedType.getActualTypeArguments();
        if (typeArgs.length != 1 || !(typeArgs[0] instanceof Class<?> typeArgClass))
            return null;

        return typeArgClass;
    }

    public static void addUndoLoopPrevention(@NotNull Class<? extends UndoableEvent> clazz, @Nullable Object... args) {
        long identifier = computeHash(clazz, args);
        if (DebugLogging.isEventEnabled())
            debugLogger.debug("Adding a UndoLoopFix, as event {} was marked to be undone", clazz.getSimpleName());

        long now = System.currentTimeMillis();
        undoLoopFixCache.entrySet().removeIf(entry -> (now - entry.getValue()) >= UNDO_PREVENTION_TTL_MS);
        undoLoopFixCache.put(identifier, now);
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

            Class<?> genericFilter = extractGenericFilter(method);

            if (DebugLogging.isBasicEnabled())
                debugLogger.debug("Registered Listener Method {}({}) of {}",
                        method.getName(), eventClass.getName(), listener.getClass());

            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle handle = lookup.unreflect(method);

                int slot = handler.priority().getSlot();
                RegisteredListener newListener = new RegisteredListener(slot, listener, plugin, handle, genericFilter);

                List<RegisteredListener> handlerList = listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>());

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

    private static long computeHash(@NotNull Class<?> clazz, @Nullable Object... args) {
        long hash = clazz.getName().hashCode();
        for (Object arg : args) {
            hash = 31 * hash + (arg == null ? 0 : arg.hashCode());
        }
        return hash;
    }

    public void runCallbackAsync(@NotNull Event event) {
        asyncEventExecutor.submit(() -> callEvent(event));
    }

    public void runCallbackAsync(@NotNull UndoableEvent event,
                                 @NotNull Consumer<UndoableEvent> undoRoutine) {
        asyncEventExecutor.submit(() -> {
            callEvent(event);
            undoRoutine.accept(event);
        });
    }

    /**
     * All event interfaces an event implements, including the ones they extend (e.g. {@code UndoableEvent}, {@code Event}).
     */
    @NotNull
    private static Set<Class<? extends Event>> eventTypes(@NotNull Class<?> eventClass) {
        Set<Class<? extends Event>> types = new LinkedHashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>();
        for (Class<?> c = eventClass; c != null; c = c.getSuperclass()) queue.addAll(List.of(c.getInterfaces()));
        while (!queue.isEmpty()) {
            Class<?> type = queue.poll();
            if (!Event.class.isAssignableFrom(type) || !types.add(type.asSubclass(Event.class))) continue;
            queue.addAll(List.of(type.getInterfaces()));
        }
        return types;
    }

    public void callEvent(@NotNull Event event) {
        Set<Class<? extends Event>> eventTypes = eventTypes(event.getClass());
        if (eventTypes.isEmpty()) throw new RuntimeException("The called Event doesn't implement Event");
        if (DebugLogging.isEventEnabled()) debugLogger.debug("Calling event {}", event.getClass().getName());

        List<RegisteredListener> handlers = new ArrayList<>();
        for (Class<? extends Event> type : eventTypes) {
            List<RegisteredListener> registered = listeners.get(type);
            if (registered != null) handlers.addAll(registered);
        }

        if (handlers.isEmpty())
            return;

        // Stable, so listeners of the same priority keep their registration order
        handlers.sort(Comparator.comparingInt(RegisteredListener::slot));

        if (DebugLogging.isEventEnabled()) debugLogger.debug("Found {} handlers for {}", handlers.size(), event.getClass().getName());

        for (RegisteredListener handler : handlers) {
            if (handler.genericFilter() != null
                    && event instanceof PackageSentEvent<?> packageSentEvent
                    && !handler.genericFilter().isInstance(packageSentEvent.getPackage())) {
                continue;
            }

            // Restored afterwards, as events can be called from within other event handlers
            Plugin previous = InternalPluginManager.setContextPlugin(handler.plugin());
            try {
                if (DebugLogging.isEventEnabled()) debugLogger.debug("Calling handler {} for {}", handler.methodHandle(), event.getClass().getName());
                handler.methodHandle().invoke(handler.listener(), event);
            } catch (Throwable e) {
                if (e instanceof VirtualMachineError error && !(error instanceof StackOverflowError)) throw error;
                if (!DebugLogging.isBasicEnabled())
                    StackTraces.trimStackTrace(e);

                // The plugin may have been unloaded in the meantime
                Plugin.PluginMetadata metadata = Comitas.getPluginManager().lookupPlugin(handler.plugin());
                logger.error("An error occurred in plugin {}", metadata == null ? "(unloaded)" : metadata.name(), e);
            } finally {
                InternalPluginManager.restoreContextPlugin(previous);
            }
        }
    }

    public void clearEventListeners() {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Clearing {} event listeners", listeners.size());
        listeners.clear();
    }

    public void shutdown() {
        if (DebugLogging.isBasicEnabled())
            debugLogger.debug("Shutting down async event executor");
        asyncEventExecutor.shutdown();
        try {
            if (!asyncEventExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                asyncEventExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncEventExecutor.shutdownNow();
        }

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Clearing all Event Listeners");
        clearEventListeners();
    }

    private record RegisteredListener(
            int slot,
            Listener listener,
            Plugin plugin,
            MethodHandle methodHandle,
            @Nullable Class<?> genericFilter
    ) {
    }
}