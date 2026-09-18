package cloud.thehsi.ComitasBotJ.Utility;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class StackTraces {
    public static void trimStackTrace(@NotNull Throwable throwable) {
        StackTraceElement[] stack = throwable.getStackTrace();

        int end = stack.length;

        for (int i = 0; i < stack.length; i++) {
            if (
                    stack[i].getClassName().startsWith("net.dv8tion.jda") ||
                    Objects.requireNonNullElse(
                            stack[i].getClassLoaderName(), ""
                    ).equals(StackTraces.class.getClassLoader().getName())
            ) {
                end = i;
                break;
            }
        }

        throwable.setStackTrace(Arrays.copyOf(stack, end));
    }
}
