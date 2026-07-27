package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

@SuppressWarnings("unused")
public interface PersistentDataStorage {
    /**
     * Set a key in this storage to some data.
     *
     * @param key The data address
     * @param type The type of the data
     * @param value The data
     */
    <T> void set(
            String key,
            PersistentDataType<T> type,
            T value
    );

    /**
     * Retrieve data from this storage.
     *
     * @param key The data address
     * @param type The type of the data
     */
    <T> T get(
            String key,
            PersistentDataType<T> type
    );

    /**
     * Does this Storage have this key?
     *
     * @param key The key
     * @return Does this Storage have this key?
     */
    boolean has(
            String key
    );

    /**
     * Does this Storage have this key of that type?
     *
     * @param key The key
     * @param type The type
     * @return Does this Storage have this key of that type?
     */
    <T> boolean has(
            String key,
            PersistentDataType<T> type
    );

    /**
     * Remove a key and its data from the storage
     *
     * @param key The key
     */
    void remove(
            String key
    );
}