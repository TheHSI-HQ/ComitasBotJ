package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a task being executed by the scheduler
 */
@SuppressWarnings("unused")
public class AsyncLaterTask implements Task {
    final int taskId;
    final @NotNull Plugin owner;
    final @NotNull ScheduledFuture<?> scheduledFuture;
    boolean canceled = false;

    public AsyncLaterTask(int taskId, @NotNull Plugin owner, @NotNull ScheduledExecutorService exec, @NotNull Runnable runnable, long delay, @NotNull TimeUnit timeUnit) {
        this.taskId = taskId;
        this.owner = owner;

        this.scheduledFuture = exec.schedule(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                owner.getLogger().error("Error in async later task #{}", taskId, e);
            }
        }, delay, timeUnit);
    }

    @Override
    public int getTaskId() {
        return taskId;
    }

    @Override
    @NotNull
    public Plugin getOwner() {
        return owner;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void cancel() {
        canceled = true;
        scheduledFuture.cancel(true);
    }
}