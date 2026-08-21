package cloud.thehsi.ComitasBotJ.API.Event.Events;

public interface UndoableEvent {
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
