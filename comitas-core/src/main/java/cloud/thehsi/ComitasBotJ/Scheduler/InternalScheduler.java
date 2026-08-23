package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InternalScheduler implements Scheduler {
    @NotNull
    final ScheduledExecutorService exec = Executors.newScheduledThreadPool(4);

    @NotNull
    final Map<Integer, ScheduledTask> tasks = new HashMap<>();
    int nextTaskId = 0;

    @Override
    public @NotNull Task runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        DebugLogging.action(plugin, runnable);
        Task task = new AsyncNowTask(nextTaskId, plugin, exec, runnable);
        tasks.put(nextTaskId++, new ScheduledTask(task, runnable));
        return task;
    }

    @Override
    public @NotNull Task runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long interval, @NotNull TimeUnit timeUnit) {
        DebugLogging.action(plugin, runnable, delay, interval, timeUnit);
        Task task = new AsyncRepeatingTask(nextTaskId, plugin, exec, runnable, delay, interval, timeUnit);
        tasks.put(nextTaskId++, new ScheduledTask(task, runnable));
        return task;
    }

    @Override
    public @NotNull Task runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, @NotNull TimeUnit timeUnit) {
        DebugLogging.action(plugin, runnable, delay, timeUnit);
        Task task = new AsyncLaterTask(nextTaskId, plugin, exec, runnable, delay, timeUnit);
        tasks.put(nextTaskId++, new ScheduledTask(task, runnable));
        return task;
    }

    public void cancelAll() {
        for (ScheduledTask task : tasks.values()) {
            task.task().cancel();
        }
    }

    record ScheduledTask(Task task, Runnable runnable) {
    }
}
