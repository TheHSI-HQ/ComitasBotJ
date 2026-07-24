package cloud.thehsi.ComitasBotJ.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Task;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a task being executed by the scheduler
 */
@SuppressWarnings("unused")
public class AsyncTask implements Task {
    final int taskId;
    final Plugin owner;
    final Thread thread;
    boolean canceled = false;

    public AsyncTask(int taskId, Plugin owner, Thread thread) {
        this.taskId = taskId;
        this.owner = owner;
        this.thread = thread;
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
        thread.interrupt();
    }
}