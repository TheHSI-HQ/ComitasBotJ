package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

import java.lang.reflect.Array;
import java.util.Base64;
import java.util.List;

public final class PersistentDataCoercion {
    private PersistentDataCoercion() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T coerce(Object value, Class<T> target) {
        if (value == null) return null;

        if (target.isInstance(value)) {
            return (T) value;
        }

        // byte[] commonly comes back as a base64 string from JSON libs
        if (target == byte[].class && value instanceof String s) {
            return (T) Base64.getDecoder().decode(s);
        }

        if (target.isArray()) {
            return (T) coerceArray(value, target.getComponentType());
        }

        return (T) coerceScalar(value, target);
    }

    private static Object coerceArray(Object value, Class<?> componentType) {
        int length;
        java.util.function.IntFunction<Object> getter;

        if (value instanceof List<?> list) {
            length = list.size();
            getter = list::get;
        } else if (value.getClass().isArray()) {
            length = Array.getLength(value);
            getter = i -> Array.get(value, i);
        } else {
            throw new IllegalArgumentException(
                    "Cannot coerce " + value.getClass() + " into array of " + componentType
            );
        }

        Object result = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Array.set(result, i, coerceScalar(getter.apply(i), componentType));
        }
        return result;
    }

    private static Object coerceScalar(Object value, Class<?> target) {
        if (value == null) return null;
        Class<?> t = wrap(target);
        if (t.isInstance(value)) return value;

        if (t == Long.class) {
            if (value instanceof Number n) return n.longValue();
            if (value instanceof String s) return Long.parseLong(s);
        } else if (t == Integer.class) {
            if (value instanceof Number n) return n.intValue();
            if (value instanceof String s) return Integer.parseInt(s);
        } else if (t == Double.class) {
            if (value instanceof Number n) return n.doubleValue();
            if (value instanceof String s) return Double.parseDouble(s);
        } else if (t == Short.class) {
            if (value instanceof Number n) return n.shortValue();
            if (value instanceof String s) return Short.parseShort(s);
        } else if (t == Byte.class) {
            if (value instanceof Number n) return n.byteValue();
            if (value instanceof String s) return Byte.parseByte(s);
        } else if (t == Boolean.class) {
            //noinspection IfCanBeSwitch // Switch statement is less clean than if statements
            if (value instanceof Boolean b) return b;
            if (value instanceof String s) return Boolean.parseBoolean(s);
            if (value instanceof Number n) return n.longValue() != 0;
        } else if (t == String.class) {
            return String.valueOf(value);
        }

        throw new IllegalArgumentException(
                "Cannot coerce " + value.getClass() + " (" + value + ") to " + target
        );
    }

    private static Class<?> wrap(Class<?> c) {
        if (!c.isPrimitive()) return c;
        if (c == long.class) return Long.class;
        if (c == int.class) return Integer.class;
        if (c == double.class) return Double.class;
        if (c == short.class) return Short.class;
        if (c == byte.class) return Byte.class;
        if (c == boolean.class) return Boolean.class;
        if (c == float.class) return Float.class;
        if (c == char.class) return Character.class;
        return c;
    }
}