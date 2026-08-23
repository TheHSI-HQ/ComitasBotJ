package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Attributes;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

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
    @NotNull
    ThreadChannel createThread(@NotNull String title);

    /**
     * Create a thread without an initial message
     *
     * @param title     The thread title
     * @param isPrivate Is this thread private
     * @return The created thread
     */
    @NotNull
    ThreadChannel createThread(@NotNull String title, boolean isPrivate);
}
