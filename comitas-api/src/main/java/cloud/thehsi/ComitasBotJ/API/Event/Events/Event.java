package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;

public interface Event {
    /**
     * What caused this event (Undo / External)
     *
     * @return The event origin
     */
    default EventOrigin getOrigin() {
        return EventOrigin.EXTERNAL;
    }
}