package cloud.thehsi.ComitasBotJ;

import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() {
        return new String[] {
                "ComitasBotJ " + Main.getServerVersion(),
                "API " + Main.getAPIVersion()
        };
    }
}
