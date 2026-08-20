package cloud.thehsi.ComitasBotJ.Plugin.PersistentData;

import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataType;
import cloud.thehsi.ComitasBotJ.DebugLogging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InternalPersistentDataStorage implements PersistentDataStorage {
    private Map<String, Entry> data = new HashMap<>();

    public InternalPersistentDataStorage() {
    }

    public InternalPersistentDataStorage(Map<String, Entry> data) {
        this.data = data;
    }

    @Override
    public <T> void set(
            String key,
            PersistentDataType<T> type,
            T value
    ) {
        DebugLogging.action(key, type, value);
        data.put(
                key,
                new Entry(
                        type.getName(),
                        type.serialize(value)
                )
        );
    }

    @Override
    public <T> T get(
            String key,
            PersistentDataType<T> type
    ) {
        DebugLogging.action(key, type);
        Entry value = data.get(key);

        if (value == null) {
            return null;
        }

        return type.deserialize(value.value);
    }

    @Override
    public boolean has(String key) {
        DebugLogging.action(key);
        return data.containsKey(key);
    }

    @Override
    public <T> boolean has(String key, PersistentDataType<T> type) {
        DebugLogging.action(key, type);
        Entry value = data.get(key);

        if (value == null)
            return false;

        return Objects.equals(value.type, type.getName());
    }

    @Override
    public void remove(String key) {
        DebugLogging.action(key);
        data.remove(key);
    }

    public Map<String, Entry> getData() {
        DebugLogging.action();
        return this.data;
    }

    public record Entry(
            String type,
            Object value
    ) {
    }
}
