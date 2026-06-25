package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InternalScheduler implements Scheduler {
    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    record ScheduledTask(Task task, Runnable runnable) {
    }

    Map<Integer, ScheduledTask> tasks = new HashMap<>();
    int nextTaskId = 0;

    @Override
    public Task runTask(Plugin plugin, Runnable runnable) {
        Task task = new SyncTask(nextTaskId, plugin);
        tasks.put(nextTaskId++, new ScheduledTask(task, runnable));
        runnable.run();
        return task;
    }

    @Override
    public Task runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        Thread thread = new Thread(runnable);
        Task task = new AsyncTask(nextTaskId, plugin, thread);
        tasks.put(nextTaskId++, new ScheduledTask(task, runnable));
        thread.start();
        return task;
    }

    @Override
    public Task runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long interval) {
        Task task = new RepeatingTask(nextTaskId, plugin);
        exec.scheduleAtFixedRate(() -> {
            if (task.isCancelled()) {
                return;
            }
            runnable.run();
        }, delay, interval, TimeUnit.MILLISECONDS);

        return task;
    }
}
