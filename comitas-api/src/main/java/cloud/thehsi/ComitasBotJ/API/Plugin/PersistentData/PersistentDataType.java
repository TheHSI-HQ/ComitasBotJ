package cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface PersistentDataType<T> {
    @NotNull Object serialize(@NotNull T value);

    @NotNull T deserialize(@NotNull Object value);

    @NotNull String getName();
}