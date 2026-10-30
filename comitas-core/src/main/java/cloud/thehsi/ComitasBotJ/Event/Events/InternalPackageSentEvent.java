package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Event.Events.PackageSentEvent;
import cloud.thehsi.ComitasBotJ.API.Plugin.Package;
import org.jetbrains.annotations.NotNull;

public class InternalPackageSentEvent<T extends Package> implements PackageSentEvent<T> {
    @NotNull T aPackage;

    public InternalPackageSentEvent(@NotNull T aPackage) {
        this.aPackage = aPackage;
    }

    @Override
    public @NotNull T getPackage() {
        return aPackage;
    }
}
