package cloud.thehsi.ComitasBotJ.Plugin.PersistentData;

import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InternalPersistentDataStorage implements PersistentDataStorage {
    private Map<String, Entry> data = new HashMap<>();

    public InternalPersistentDataStorage() {}

    public InternalPersistentDataStorage(Map<String, Entry> data) {
        this.data = data;
    }

    @Override
    public <T> void set(
            String key,
            PersistentDataType<T> type,
            T value
    ) {
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
        Entry value = data.get(key);

        if (value == null) {
            return null;
        }

        return type.deserialize(value.value);
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    public <T> boolean has(String key, PersistentDataType<T> type) {
        Entry value = data.get(key);

        if (value == null)
            return false;

        return Objects.equals(value.type, type.getName());
    }

    public Map<String, Entry> getData() {
        return this.data;
    }

    public record Entry(
            String type,
            Object value
    ) {
    }
}
