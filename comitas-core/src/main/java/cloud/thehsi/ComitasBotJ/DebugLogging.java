package cloud.thehsi.ComitasBotJ;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class DebugLogging {
    /*
    --verbose levels

    0 -> None
    1 -> Basic (startup and loading and shutdown)
    2 -> Events (Events being triggered, and info about them)
    3 -> Actions (Everything the plugins do, like sending a message, listing channels, etc...)
    4 -> API Debug (Debug logging on the discord api itself)
     */

    public static int getVerbosityLevel() {
        return Main.debugLevel;
    }

    public static Logger getLogger() {
        Class<?> caller = StackWalker.getInstance(
                        StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .skip(1) // skip getLogger()
                        .findFirst()
                        .map(StackWalker.StackFrame::getDeclaringClass)
                        .orElseThrow());

        Logger l = (Logger) LoggerFactory.getLogger(caller);
        l.setLevel(Level.DEBUG);
        return l;
    }

    public static boolean isBasicEnabled() {
        return getVerbosityLevel() >= 1;
    }

    public static boolean isEventEnabled() {
        return getVerbosityLevel() >= 2;
    }

    public static boolean isActionEnabled() {
        return getVerbosityLevel() >= 3;
    }

    public static boolean isAPIEnabled() {
        return getVerbosityLevel() >= 4;
    }
}
