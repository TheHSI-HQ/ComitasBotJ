package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Plugin.Package;
import org.jetbrains.annotations.NotNull;

/**
 * Listen for any sent packages of the specified package type
 *
 * @param <T> The package type to listen for
 */
public interface PackageSentEvent<T extends Package> extends Event {
    /**
     * @return The package that triggered this event
     */
    @NotNull
    T getPackage();
}
