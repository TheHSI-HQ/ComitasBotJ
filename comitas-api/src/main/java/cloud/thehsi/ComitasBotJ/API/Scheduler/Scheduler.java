package cloud.thehsi.ComitasBotJ.API.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

@SuppressWarnings("unused")
public interface Scheduler {
    /**
     * Run a task synchronously
     *
     * @return The task
     */
    Task runTask(Plugin plugin, Runnable runnable);

    /**
     * Run a task asynchronously
     *
     * @return The task
     */
    Task runTaskAsynchronously(Plugin plugin, Runnable runnable);

    /**
     * Run a task asynchronously repeatedly
     *
     * @param delayMS    Delay between now and first execution in milliseconds
     * @param intervalMS Delay between evey execution in milliseconds
     * @return The task
     */
    Task runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delayMS, long intervalMS);

    /**
     * Run a task asynchronously later
     *
     * @param delayMS Delay between now and the execution in milliseconds
     * @return The task
     */
    Task runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delayMS);
}
