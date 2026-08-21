package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Event.Events.UndoableEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

public class InternalUndoableEvent implements UndoableEvent {
    boolean willUndo = false;

    @Override
    public boolean willUndo() {
        DebugLogging.action();
        return willUndo;
    }

    @Override
    public void setUndo(boolean undo) {
        DebugLogging.action();
        willUndo = undo;
    }

    @Override
    public void undo() {
        DebugLogging.action();
        willUndo = true;
    }
}
