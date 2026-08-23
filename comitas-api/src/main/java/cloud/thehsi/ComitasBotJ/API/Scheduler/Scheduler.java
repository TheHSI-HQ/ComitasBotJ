package cloud.thehsi.ComitasBotJ.API.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public interface Scheduler {
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
     * @param delay      Delay between now and first execution
     * @param interval   Delay between evey execution
     * @param timeUnit   The unit in which the delay is messured
     * @return The task
     */
    @NotNull
    Task runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long interval, @NotNull TimeUnit timeUnit);

    /**
     * Run a task asynchronously later
     *
     * @param delay     Delay between now and the execution in milliseconds
     * @param timeUnit  he unit in which the delay is messured
     * @return The task
     */
    @NotNull
    Task runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, @NotNull TimeUnit timeUnit);
}
