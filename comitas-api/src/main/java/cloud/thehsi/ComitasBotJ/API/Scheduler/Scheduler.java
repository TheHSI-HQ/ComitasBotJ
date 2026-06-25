package cloud.thehsi.ComitasBotJ.API.Scheduler;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

@SuppressWarnings("unused")
public interface Scheduler {
    Task runTask(Plugin plugin, Runnable runnable);

    Task runTaskAsynchronously(Plugin plugin, Runnable runnable);

    Task runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long interval);
}
