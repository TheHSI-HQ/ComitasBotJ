package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.concurrent.ScheduledFuture;

public class InternalScheduler implements Scheduler {
    @NotNull
    final ScheduledExecutorService exec = Executors.newScheduledThreadPool(4, r -> {
        Thread t = new Thread(r, "ComitasScheduler");
        t.setDaemon(true);
        return t;
    });

    /**
     * Tasks, which haven't finished yet. Finished and canceled tasks are removed, so they don't keep unloaded plugins alive.
     */
    @NotNull
    final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    @NotNull
    final AtomicInteger nextTaskId = new AtomicInteger();

    @NotNull
    private Task schedule(@NotNull Plugin plugin, @NotNull Runnable runnable, boolean repeating,
                          @NotNull Function<Runnable, ScheduledFuture<?>> schedule) {
        if (Comitas.getPluginManager() instanceof InternalPluginManager pluginManager)
            pluginManager.requireCaller(plugin);

        int id = nextTaskId.getAndIncrement();
        InternalTask task = new InternalTask(id, plugin, runnable, repeating, () -> tasks.remove(id));
        // Registered before it is started, so a short task can't finish (and remove itself) before it is added
        tasks.put(id, task);
        task.start(schedule);
        return task;
    }

    @Override
    public @NotNull Task runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        DebugLogging.action(plugin, runnable);
        return schedule(plugin, runnable, false, r -> exec.schedule(r, 0, TimeUnit.MILLISECONDS));
    }

    @Override
    public @NotNull Task runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long interval, @NotNull TimeUnit timeUnit) {
        DebugLogging.action(plugin, runnable, delay, interval, timeUnit);
        return schedule(plugin, runnable, true, r -> exec.scheduleAtFixedRate(r, delay, interval, timeUnit));
    }

    @Override
    public @NotNull Task runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, @NotNull TimeUnit timeUnit) {
        DebugLogging.action(plugin, runnable, delay, timeUnit);
        return schedule(plugin, runnable, false, r -> exec.schedule(r, delay, timeUnit));
    }

    /**
     * Cancels the tasks of every plugin, except the given one (the base plugin, which isn't reloaded).
     */
    public void cancelAllExcept(@Nullable Plugin keep) {
        for (Task task : tasks.values())
            if (task.getOwner() != keep) task.cancel();
    }

    public void cancelAll() {
        cancelAllExcept(null);
    }

    public void shutdown() {
        cancelAll();
        exec.shutdownNow();
    }
}
