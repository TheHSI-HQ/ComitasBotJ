package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a task being executed by the scheduler
 */
@SuppressWarnings("unused")
public class SyncTask implements Task {
    int taskId;
    Plugin owner;

    public SyncTask(int taskId, Plugin owner) {
        this.taskId = taskId;
        this.owner = owner;
    }

    public int getTaskId() {
        return taskId;
    }

    public @NotNull Plugin getOwner() {
        return owner;
    }

    public boolean isSync() {
        return true;
    }

    public boolean isCancelled() {
        return false;
    }

    public void cancel() {
    }
}