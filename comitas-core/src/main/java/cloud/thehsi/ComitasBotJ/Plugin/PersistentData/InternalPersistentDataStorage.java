package cloud.thehsi.ComitasBotJ.Plugin.PersistentData;

import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataType;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

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
                        plainCopy(type.serialize(value), type.getName(), 0)
                )
        );
    }

    /**
     * Deep copies serialized plugin data, only allowing JSON like values.
     * <p>
     * The data is serialized with Gson, which reflects on every object. A custom {@link PersistentDataType}
     * returning arbitrary objects (e.g. Comitas internals) could otherwise make Gson dump internal state.
     * Copying also prevents collections from returning different elements later on.
     */
    @Nullable
    private static Object plainCopy(@Nullable Object value, @NotNull String typeName, int depth) {
        if (depth > 64)
            throw new IllegalArgumentException("PersistentDataType " + typeName + " produced too deeply nested data");

        if (value == null || value instanceof String || value instanceof Boolean || value instanceof Character)
            return value;
        if (value instanceof Number && value.getClass().getClassLoader() == null)
            return value; // JDK numbers only

        Class<?> type = value.getClass();
        if (type.isArray()) {
            Class<?> component = type.getComponentType();
            if (component.isPrimitive()) {
                int length = Array.getLength(value);
                Object copy = Array.newInstance(component, length);
                System.arraycopy(value, 0, copy, 0, length);
                return copy;
            }
            Object[] source = (Object[]) value;
            Object[] copy = (Object[]) Array.newInstance(
                    component.getClassLoader() == null ? component : Object.class, source.length);
            for (int i = 0; i < source.length; i++) copy[i] = plainCopy(source[i], typeName, depth + 1);
            return copy;
        }

        if (value instanceof Collection<?> collection) {
            List<Object> copy = new ArrayList<>();
            for (Object element : collection.toArray()) copy.add(plainCopy(element, typeName, depth + 1));
            return copy;
        }

        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : List.copyOf(map.entrySet()))
                copy.put(plainCopy(entry.getKey(), typeName, depth + 1), plainCopy(entry.getValue(), typeName, depth + 1));
            return copy;
        }

        throw new IllegalArgumentException("PersistentDataType " + typeName + " serialized to the unsupported type "
                + type.getName() + ", only strings, numbers, booleans, arrays, lists and maps can be stored");
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
