package cloud.thehsi.ComitasBotJ.API.Event;

/**
 * Defines what trigerred an event
 */
public enum EventOrigin {
    /**
     * External Event Origin, triggered by Discord or Comitas
     */
    EXTERNAL,
    /**
     * Undo Event Origin, triggered when an event gets undone, triggering a followup event
     */
    UNDO
}
