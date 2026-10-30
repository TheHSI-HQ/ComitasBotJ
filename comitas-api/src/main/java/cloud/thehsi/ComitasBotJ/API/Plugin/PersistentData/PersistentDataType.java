package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a type of data storable in persistent storage
 *
 * @param <T> The raw data type
 */
@SuppressWarnings("unused")
public interface PersistentDataType<T> {
    /**
     * Searialize the input data
     *
     * @param value The data to serialize
     * @return The serialized data, which may only consist of strings, numbers, booleans, arrays, lists and maps
     */
    @NotNull Object serialize(@NotNull T value);

    /**
     * Deserialize the input data
     *
     * @param value The serialized data
     * @return The deserialized data
     */
    @NotNull T deserialize(@NotNull Object value);

    /**
     * @return The types name
     */
    @NotNull String getName();
}