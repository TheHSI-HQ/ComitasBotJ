package cloud.thehsi.ComitasBotJ.API.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Scheduler {
    /**
     * Run a task synchronously
     *
     * @return The task
     */
    @NotNull
    Task runTask(@NotNull Plugin plugin, @NotNull Runnable runnable);

    /**
     * Run a task asynchronously
     *
     * @return The task
     */
    @NotNull
    Task runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable);

    /**
     * Run a task asynchronously repeatedly
     *
     * @param delayMS    Delay between now and first execution in milliseconds
     * @param intervalMS Delay between evey execution in milliseconds
     * @return The task
     */
    @NotNull
    Task runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delayMS, long intervalMS);

    /**
     * Run a task asynchronously later
     *
     * @param delayMS Delay between now and the execution in milliseconds
     * @return The task
     */
    @NotNull
    Task runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delayMS);
}
