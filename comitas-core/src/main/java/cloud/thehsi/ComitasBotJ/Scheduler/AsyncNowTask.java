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
public class AsyncNowTask implements Task {
    final int taskId;
    final @NotNull Plugin owner;
    final @NotNull ScheduledFuture<?> scheduledFuture;
    boolean canceled = false;

    public AsyncNowTask(int taskId, @NotNull Plugin owner, @NotNull ScheduledExecutorService exec, @NotNull Runnable runnable) {
        this.taskId = taskId;
        this.owner = owner;

        this.scheduledFuture = exec.schedule(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                owner.getLogger().error("Error in async now task #{}", taskId, e);
            }
        }, 0, TimeUnit.MILLISECONDS);
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
    public boolean isSync() {
        return false;
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