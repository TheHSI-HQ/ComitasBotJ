package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public final class PersistentDataTypes {
    public @NotNull
    static final PersistentDataType<String> STRING = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@NotNull String value) {
            return value;
        }

        @Override
        public @NotNull String deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, String.class));
        }

        @Override
        public @NotNull String getName() {
            return "STRING";
        }
    };

    public @NotNull
    static final PersistentDataType<Integer> INTEGER = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@NotNull Integer value) {
            return value;
        }

        @Override
        public @NotNull Integer deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Integer.class));
        }

        @Override
        public @NotNull String getName() {
            return "INTEGER";
        }
    };

    public @NotNull
    static final PersistentDataType<Long> LONG = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@NotNull Long value) {
            return value;
        }

        @Override
        public @NotNull Long deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Long.class));
        }

        @Override
        public @NotNull String getName() {
            return "LONG";
        }
    };

    public @NotNull
    static final PersistentDataType<Double> DOUBLE = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@NotNull Double value) {
            return value;
        }

        @Override
        public @NotNull Double deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Double.class));
        }

        @Override
        public @NotNull String getName() {
            return "DOUBLE";
        }
    };

    public @NotNull
    static final PersistentDataType<Byte> BYTE = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@NotNull Byte value) {
            return value;
        }

        @Override
        public @NotNull Byte deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Byte.class));
        }

        @Override
        public @NotNull String getName() {
            return "BYTE";
        }
    };

    public @NotNull
    static final PersistentDataType<Short> SHORT = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@NotNull Short value) {
            return value;
        }

        @Override
        public @NotNull Short deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Short.class));
        }

        @Override
        public @NotNull String getName() {
            return "SHORT";
        }
    };

    public @NotNull
    static final PersistentDataType<Boolean> BOOLEAN = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@NotNull Boolean value) {
            return value;
        }

        @Override
        public @NotNull Boolean deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Boolean.class));
        }

        @Override
        public @NotNull String getName() {
            return "BOOLEAN";
        }
    };

    public @NotNull
    static final PersistentDataType<byte[]> BYTE_ARRAY = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(byte @NotNull [] value) {
            return value.clone();
        }

        @Override
        public byte @NotNull [] deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, byte[].class));
        }

        @Override
        public @NotNull String getName() {
            return "BYTE_ARRAY";
        }
    };

    public @NotNull
    static final PersistentDataType<String[]> STRING_ARRAY = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@Nullable String @NotNull [] value) {
            return value.clone();
        }

        @Override
        public @NotNull String @NotNull [] deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, String[].class));
        }

        @Override
        public @NotNull String getName() {
            return "STRING_ARRAY";
        }
    };

    public @NotNull
    static final PersistentDataType<Integer[]> INTEGER_ARRAY = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@Nullable Integer @NotNull [] value) {
            return value.clone();
        }

        @Override
        public @NotNull Integer @NotNull [] deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Integer[].class));
        }

        @Override
        public @NotNull String getName() {
            return "INTEGER_ARRAY";
        }
    };

    public @NotNull
    static final PersistentDataType<Long[]> LONG_ARRAY = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@Nullable Long @NotNull [] value) {
            return value.clone();
        }

        @Override
        public @NotNull Long @NotNull [] deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Long[].class));
        }

        @Override
        public @NotNull String getName() {
            return "LONG_ARRAY";
        }
    };

    public @NotNull
    static final PersistentDataType<Double[]> DOUBLE_ARRAY = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@Nullable Double @NotNull [] value) {
            return value.clone();
        }

        @Override
        public @NotNull Double @NotNull [] deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Double[].class));
        }

        @Override
        public @NotNull String getName() {
            return "DOUBLE_ARRAY";
        }
    };

    public @NotNull
    static final PersistentDataType<Short[]> SHORT_ARRAY = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@Nullable Short @NotNull [] value) {
            return value.clone();
        }

        @Override
        public @NotNull Short @NotNull [] deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Short[].class));
        }

        @Override
        public @NotNull String getName() {
            return "SHORT_ARRAY";
        }
    };

    public @NotNull
    static final PersistentDataType<Boolean[]> BOOLEAN_ARRAY = new PersistentDataType<>() {
        @Override
        public @NotNull Object serialize(@Nullable Boolean @NotNull [] value) {
            return value.clone();
        }

        @Override
        public @NotNull Boolean @NotNull [] deserialize(@NotNull Object value) {
            return Objects.requireNonNull(PersistentDataCoercion.coerce(value, Boolean[].class));
        }

        @Override
        public @NotNull String getName() {
            return "BOOLEAN_ARRAY";
        }
    };

    private @NotNull
    static final Map<String, PersistentDataType<?>> TYPES =
            new HashMap<>();

    static {
        register("STRING", PersistentDataTypes.STRING);
        register("INTEGER", PersistentDataTypes.INTEGER);
        register("LONG", PersistentDataTypes.LONG);
        register("DOUBLE", PersistentDataTypes.DOUBLE);
        register("BYTE", PersistentDataTypes.BYTE);
        register("SHORT", PersistentDataTypes.SHORT);
        register("BOOLEAN", PersistentDataTypes.BOOLEAN);

        register("BYTE_ARRAY", PersistentDataTypes.BYTE_ARRAY);
        register("STRING_ARRAY", PersistentDataTypes.STRING_ARRAY);
        register("INTEGER_ARRAY", PersistentDataTypes.INTEGER_ARRAY);
        register("LONG_ARRAY", PersistentDataTypes.LONG_ARRAY);
        register("DOUBLE_ARRAY", PersistentDataTypes.DOUBLE_ARRAY);
        register("SHORT_ARRAY", PersistentDataTypes.SHORT_ARRAY);
        register("BOOLEAN_ARRAY", PersistentDataTypes.BOOLEAN_ARRAY);
    }

    private static void register(
            @NotNull String name,
            @NotNull PersistentDataType<?> type
    ) {
        TYPES.put(name, type);
    }

    @NotNull
    public static PersistentDataType<?> get(@NotNull String name) {
        return TYPES.get(name);
    }
}
