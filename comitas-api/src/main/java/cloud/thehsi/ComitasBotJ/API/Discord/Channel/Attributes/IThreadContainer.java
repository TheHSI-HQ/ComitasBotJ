package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Attributes;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A channel, which may contain threads
 */
public interface IThreadContainer {
    /**
     * Lists all threads
     *
     * @return Every thread in this channel
     */
    @NotNull
    @Unmodifiable
    List<ThreadChannel> getThreads();

    /**
     * Create a thread without an initial message
     *
     * @param title The thread title
     * @return The created thread
     */
    @RequiresBotPermission(Permission.CREATE_PUBLIC_THREADS)
    @NotNull
    ThreadChannel createThread(@NotNull String title);

    /**
     * Create a thread without an initial message
     *
     * @param title     The thread title
     * @param isPrivate Is this thread private
     * @return The created thread
     */
    @RequiresBotPermission(conditional = {Permission.CREATE_PUBLIC_THREADS, Permission.CREATE_PRIVATE_THREADS})
    @NotNull
    ThreadChannel createThread(@NotNull String title, boolean isPrivate);
}
