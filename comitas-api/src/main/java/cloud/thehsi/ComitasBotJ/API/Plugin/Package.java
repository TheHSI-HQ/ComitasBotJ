package cloud.thehsi.ComitasBotJ.API.Plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an extendable Package,
 * used for inter plugin communications
 */
public interface Package {
    /**
     * Returns the package identifier,
     * which should be a unique string,
     * used to identify the package.
     *
     * @return The package identifier
     */
    @NotNull
    String getIdentifier();
}
