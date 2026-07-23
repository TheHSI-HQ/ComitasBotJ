package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

@SuppressWarnings("unused")
public interface PersistentDataType<T> {
    Object serialize(T value);

    T deserialize(Object value);

    String getName();
}