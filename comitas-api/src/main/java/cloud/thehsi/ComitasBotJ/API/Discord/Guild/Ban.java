package cloud.thehsi.ComitasBotJ.API.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface Ban {
    User user();

    @Nullable
    String reason();

    void unban();
}
