package cloud.thehsi.ComitasBotJ.Plugin.PersistentData;

import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataType;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InternalPersistentDataStorage implements PersistentDataStorage {
    private @NotNull Map<String, Entry> data = new HashMap<>();

    public InternalPersistentDataStorage() {
    }

    public InternalPersistentDataStorage(@NotNull Map<String, Entry> data) {
        this.data = data;
    }

    @Override
    public <T> void set(
            @NotNull String key,
            @NotNull PersistentDataType<T> type,
            @NotNull T value
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
    @Nullable
    public <T> T get(
            @NotNull String key,
            @NotNull PersistentDataType<T> type
    ) {
        DebugLogging.action(key, type);
        Entry value = data.get(key);

        if (value == null) {
            return null;
        }

        return type.deserialize(value.value);
    }

    @Override
    @NotNull
    public <T> T get(@NotNull String key, @NotNull PersistentDataType<T> type, @NotNull T fallback) {
        T val = get(key, type);
        if (val == null)
            return fallback;
        return val;
    }

    @Override
    public boolean has(@NotNull String key) {
        DebugLogging.action(key);
        return data.containsKey(key);
    }

    @Override
    public <T> boolean has(@NotNull String key, @NotNull PersistentDataType<T> type) {
        DebugLogging.action(key, type);
        Entry value = data.get(key);

        if (value == null)
            return false;

        return Objects.equals(value.type, type.getName());
    }

    @Override
    public void remove(@NotNull String key) {
        DebugLogging.action(key);
        data.remove(key);
    }

    @NotNull
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
