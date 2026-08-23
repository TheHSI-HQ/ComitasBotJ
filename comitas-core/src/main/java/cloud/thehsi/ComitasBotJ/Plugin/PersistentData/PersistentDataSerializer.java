package cloud.thehsi.ComitasBotJ.Plugin.PersistentData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PersistentDataSerializer {
    private @NotNull
    static final Gson GSON = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();

    @NotNull
    public static String serializeData(
            @NotNull Map<String, InternalPersistentDataStorage.Entry> data
    ) {
        return GSON.toJson(data);
    }

    @NotNull
    public static Map<String, InternalPersistentDataStorage.Entry> deserializeData(byte[] data) {

        String json = new String(
                data,
                StandardCharsets.UTF_8
        );

        Type type = new TypeToken<
                Map<String, InternalPersistentDataStorage.Entry>
                >() {
        }.getType();

        return GSON.fromJson(json, type);
    }
}