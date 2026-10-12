package cloud.thehsi.ComitasBotJ.API.Event.Events;

/**
 * Represents an {@link Event} that can be undone
 */
public interface UndoableEvent extends Event {
    /**
     * Will this event be undone
     *
     * @return Will this event be undone
     */
    boolean willUndo();

    /**
     * Mark / Unmark the event for undoing
     *
     * @param undo Set the event's marked for undo status
     */
    void setUndo(boolean undo);

    /**
     * Mark the event for undoing
     */
    void undo();
}
