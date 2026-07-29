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
        thread.interrupt();
    }
}