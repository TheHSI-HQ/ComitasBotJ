package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;

import java.util.HashMap;
import java.util.Map;

public class InternalScheduler implements Scheduler {

    record ScheduledTask(Task task, Runnable runnable) {
    }

    final Map<Integer, ScheduledTask> tasks = new HashMap<>();
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
        Task task = new RepeatingTask(nextTaskId, plugin, runnable, delay, interval);
        tasks.put(nextTaskId++, new ScheduledTask(task, runnable));
        return task;
    }

    @Override
    public Task runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (InterruptedException ignored) {
            }
        });

        Task task = new AsyncTask(nextTaskId, plugin, thread);
        tasks.put(nextTaskId++, new ScheduledTask(task, runnable));
        thread.start();
        return task;
    }

    public void cancelAll() {
        for (ScheduledTask task : tasks.values()) {
            task.task().cancel();
        }
    }
}
