package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class, used to store data persistently across server restarts
 */
@SuppressWarnings("unused")
public interface PersistentDataStorage {
    /**
     * Set a key in this storage to some data.
     *
     * @param key   The data address
     * @param type  The type of the data
     * @param value The data
     * @param <T>   The raw type of data
     */
    <T> void set(
            @NotNull String key,
            @NotNull PersistentDataType<T> type,
            @NotNull T value
    );

    /**
     * Retrieve data from this storage.
     *
     * @param key  The data address
     * @param type The type of the data
     * @param <T>  The raw type of data
     * @return     The data, if present, else null
     */
    @Nullable
    <T> T get(
            @NotNull String key,
            @NotNull PersistentDataType<T> type
    );

    /**
     * Retrieve data from this storage.
     *
     * @param key      The data address
     * @param type     The type of the data
     * @param fallback The fallback value, in case no value is found for key
     * @param <T>      The raw type of data
     * @return         The data, if present, else fallback
     */
    @NotNull
    <T> T get(
            @NotNull String key,
            @NotNull PersistentDataType<T> type,
            @NotNull T fallback
    );

    /**
     * Does this Storage have this key?
     *
     * @param key The key
     * @return Does this Storage have this key?
     */
    boolean has(
            @NotNull String key
    );

    /**
     * Does this Storage have this key of that type?
     *
     * @param key  The key
     * @param type The type
     * @param <T>   The raw type of data
     * @return Does this Storage have this key of that type?
     */
    <T> boolean has(
            @NotNull String key,
            @NotNull PersistentDataType<T> type
    );

    /**
     * Remove a key and its data from the storage
     *
     * @param key The key
     */
    void remove(
            @NotNull String key
    );
}