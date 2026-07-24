package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a task being executed by the scheduler
 */
@SuppressWarnings("unused")
public class RepeatingTask implements Task {
    final int taskId;
    final Plugin owner;
    boolean canceled = false;
    final ScheduledFuture<?> scheduledFuture;
    final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();


    public RepeatingTask(int taskId, Plugin owner, Runnable runnable, Long delay, Long interval) {
        this.taskId = taskId;
        this.owner = owner;
        this.scheduledFuture = exec.scheduleAtFixedRate(() -> {
            if (this.isCancelled()) {
                return;
            }
            runnable.run();
        }, delay, interval, TimeUnit.MILLISECONDS);
    }

    public int getTaskId() {
        return taskId;
    }

    public @NotNull Plugin getOwner() {
        return owner;
    }

    public boolean isSync() {
        return false;
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void cancel() {
        canceled = true;
        scheduledFuture.cancel(true);
    }
}