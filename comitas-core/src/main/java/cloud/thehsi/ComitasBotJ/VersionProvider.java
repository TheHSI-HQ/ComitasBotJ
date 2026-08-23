package cloud.thehsi.ComitasBotJ;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {
    @Override
    @NotNull
    public String[] getVersion() {
        return new String[]{
                "ComitasBotJ " + Main.getServerVersion()
        };
    }
}
