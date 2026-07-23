package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class PersistentDataTypes {
    public static final PersistentDataType<String> STRING = new PersistentDataType<>() {
        @Override
        public Object serialize(String value) {
            return value;
        }

        @Override
        public String deserialize(Object value) {
            return (String) value;
        }

        @Override
        public String getName() {
            return "STRING";
        }
    };

    public static final PersistentDataType<Integer> INTEGER = new PersistentDataType<>() {
        @Override
        public Object serialize(Integer value) {
            return value;
        }

        @Override
        public Integer deserialize(Object value) {
            return (Integer) value;
        }

        @Override
        public String getName() {
            return "INTEGER";
        }
    };

    public static final PersistentDataType<Long> LONG = new PersistentDataType<>() {
        @Override
        public Object serialize(Long value) {
            return value;
        }

        @Override
        public Long deserialize(Object value) {
            return (Long) value;
        }

        @Override
        public String getName() {
            return "LONG";
        }
    };

    public static final PersistentDataType<Double> DOUBLE = new PersistentDataType<>() {
        @Override
        public Object serialize(Double value) {
            return value;
        }

        @Override
        public Double deserialize(Object value) {
            return (Double) value;
        }

        @Override
        public String getName() {
            return "DOUBLE";
        }
    };

    public static final PersistentDataType<Byte> BYTE = new PersistentDataType<>() {
        @Override
        public Object serialize(Byte value) {
            return value;
        }

        @Override
        public Byte deserialize(Object value) {
            return (Byte) value;
        }

        @Override
        public String getName() {
            return "BYTE";
        }
    };

    public static final PersistentDataType<Short> SHORT = new PersistentDataType<>() {
        @Override
        public Object serialize(Short value) {
            return value;
        }

        @Override
        public Short deserialize(Object value) {
            return (Short) value;
        }

        @Override
        public String getName() {
            return "SHORT";
        }
    };

    public static final PersistentDataType<Boolean> BOOLEAN = new PersistentDataType<>() {
        @Override
        public Object serialize(Boolean value) {
            return value;
        }

        @Override
        public Boolean deserialize(Object value) {
            return (Boolean) value;
        }

        @Override
        public String getName() {
            return "BOOLEAN";
        }
    };

    public static final PersistentDataType<byte[]> BYTE_ARRAY = new PersistentDataType<>() {
        @Override
        public Object serialize(byte[] value) {
            return value.clone();
        }

        @Override
        public byte[] deserialize(Object value) {
            return ((byte[]) value).clone();
        }

        @Override
        public String getName() {
            return "BYTE_ARRAY";
        }
    };

    public static final PersistentDataType<String[]> STRING_ARRAY = new PersistentDataType<>() {
        @Override
        public Object serialize(String[] value) {
            return value.clone();
        }

        @Override
        public String[] deserialize(Object value) {
            return ((String[]) value).clone();
        }

        @Override
        public String getName() {
            return "STRING_ARRAY";
        }
    };

    public static final PersistentDataType<Integer[]> INTEGER_ARRAY = new PersistentDataType<>() {
        @Override
        public Object serialize(Integer[] value) {
            return value.clone();
        }

        @Override
        public Integer[] deserialize(Object value) {
            return ((Integer[]) value).clone();
        }

        @Override
        public String getName() {
            return "INTEGER_ARRAY";
        }
    };

    public static final PersistentDataType<Long[]> LONG_ARRAY = new PersistentDataType<>() {
        @Override
        public Object serialize(Long[] value) {
            return value.clone();
        }

        @Override
        public Long[] deserialize(Object value) {
            return ((Long[]) value).clone();
        }

        @Override
        public String getName() {
            return "LONG_ARRAY";
        }
    };

    public static final PersistentDataType<Double[]> DOUBLE_ARRAY = new PersistentDataType<>() {
        @Override
        public Object serialize(Double[] value) {
            return value.clone();
        }

        @Override
        public Double[] deserialize(Object value) {
            return ((Double[]) value).clone();
        }

        @Override
        public String getName() {
            return "DOUBLE_ARRAY";
        }
    };

    public static final PersistentDataType<Short[]> SHORT_ARRAY = new PersistentDataType<>() {
        @Override
        public Object serialize(Short[] value) {
            return value.clone();
        }

        @Override
        public Short[] deserialize(Object value) {
            return ((Short[]) value).clone();
        }

        @Override
        public String getName() {
            return "SHORT_ARRAY";
        }
    };

    public static final PersistentDataType<Boolean[]> BOOLEAN_ARRAY = new PersistentDataType<>() {
        @Override
        public Object serialize(Boolean[] value) {
            return value.clone();
        }

        @Override
        public Boolean[] deserialize(Object value) {
            return ((Boolean[]) value).clone();
        }

        @Override
        public String getName() {
            return "BOOLEAN_ARRAY";
        }
    };

    private static final Map<String, PersistentDataType<?>> TYPES =
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
            String name,
            PersistentDataType<?> type
    ) {
        TYPES.put(name, type);
    }

    public static PersistentDataType<?> get(String name) {
        return TYPES.get(name);
    }
}
