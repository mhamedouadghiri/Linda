package linda.shell;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An abstract class defining some useful methods for shell implementation, a static @{@link ShellCallback} callback,
 * and the {@link #run()} method, the entrypoint point of all shell implementations.
 *
 * @see Shell
 * @see ShellWithFile
 */
public abstract class AbstractShell {

    protected static final Map<String, Method> lindaPrimitives = getLindaPrimitives();

    private static Map<String, Method> getLindaPrimitives() {
        return Arrays.stream(Linda.class.getMethods())
                .collect(Collectors.toMap(Method::getName, Function.identity()));
    }

    protected static String[] sanitize(String[] strings) {
        return Arrays.stream(strings)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    protected static void cleanExit(Scanner scanner, int status) {
        try {
            scanner.close();
        } catch (Exception ignored) {
        }
        try {
            System.exit(status);
        } catch (Exception ignored) {
        }
    }

    /**
     * Entrypoint to all shell implementations.
     */
    protected abstract void run();

    /**
     * A custom callback implicitly used with `eventRegister` in all shell implementations.
     */
    protected static class ShellCallback implements Callback {
        public void call(Tuple t) {
            System.out.printf(Locale.UK, "Callback triggered, got `%s`.\n", t);
        }
    }
}
