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
public class AsyncRepeatingTask implements Task {
    final int taskId;
    @NotNull
    final Plugin owner;
    @NotNull
    final ScheduledFuture<?> scheduledFuture;
    boolean canceled = false;


    public AsyncRepeatingTask(int taskId, @NotNull Plugin owner, @NotNull ScheduledExecutorService exec, @NotNull Runnable runnable, long delay, long interval, @NotNull TimeUnit timeUnit) {
        this.taskId = taskId;
        this.owner = owner;
        this.scheduledFuture = exec.scheduleAtFixedRate(() -> {
            if (this.isCancelled()) {
                return;
            }
            try {
                runnable.run();
            } catch (Exception e) {
                owner.getLogger().error("Error in async repeating task #{}", taskId, e);
            }
        }, delay, interval, timeUnit);
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