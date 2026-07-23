package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

@SuppressWarnings("unused")
public interface PersistentDataStorage {
    <T> void set(
            String key,
            PersistentDataType<T> type,
            T value
    );

    <T> T get(
            String key,
            PersistentDataType<T> type
    );
}