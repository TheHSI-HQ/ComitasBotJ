package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;

/**
 * Represents a task being executed by the scheduler
 */
final class InternalTask implements Task {
    private final int taskId;
    private final @NotNull Plugin owner;
    private final @NotNull Runnable runnable;
    private final boolean repeating;
    private final @NotNull Runnable onDone;
    private volatile boolean canceled = false;
    private volatile @Nullable ScheduledFuture<?> scheduledFuture;

    /**
     * @param repeating If the task runs more than once, otherwise it is removed after its run
     * @param onDone    Called once the task won't run anymore
     */
    InternalTask(int taskId, @NotNull Plugin owner, @NotNull Runnable runnable, boolean repeating, @NotNull Runnable onDone) {
        this.taskId = taskId;
        this.owner = owner;
        this.runnable = runnable;
        this.repeating = repeating;
        this.onDone = onDone;
    }

    /**
     * @param schedule Schedules the given runnable, returns its future
     */
    void start(@NotNull Function<Runnable, ScheduledFuture<?>> schedule) {
        String kind = repeating ? "repeating" : "async";
        this.scheduledFuture = schedule.apply(() -> {
            if (canceled) return;
            // The task runs on behalf of its owner, also for calls without plugin frames on the stack
            Plugin previous = InternalPluginManager.setContextPlugin(owner);
            try {
                runnable.run();
            } catch (Exception | LinkageError e) {
                owner.getLogger().error("Error in {} task #{}", kind, taskId, e);
            } finally {
                InternalPluginManager.restoreContextPlugin(previous);
                if (!repeating) onDone.run();
            }
        });
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
        ScheduledFuture<?> future = scheduledFuture;
        if (future != null) future.cancel(true);
        onDone.run();
    }
}
