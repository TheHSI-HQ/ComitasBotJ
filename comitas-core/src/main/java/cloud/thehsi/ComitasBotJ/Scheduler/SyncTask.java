package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a task being executed by the scheduler
 */
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class SyncTask implements Task {
    final int taskId;
    final Plugin owner;

    public SyncTask(int taskId, Plugin owner) {
        this.taskId = taskId;
        this.owner = owner;
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
        return true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void cancel() {
    }
}