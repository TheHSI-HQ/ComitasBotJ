package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import org.jetbrains.annotations.NotNull;

public interface Event {
    /**
     * What caused this event (Undo / External)
     *
     * @return The event origin
     */
    @NotNull
    default EventOrigin getOrigin() {
        return EventOrigin.EXTERNAL;
    }
}