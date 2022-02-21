package linda.shell;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.TupleFormatException;
import linda.shm.CentralizedLinda;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This allows the user to write a predefined scenario in a file that is parsed and executed against a Linda kernel.
 *
 * This makes it easier to save a particular scenario for future use, in contrast with the @{@link Shell} way.
 *
 * All the common Linda primitives defined in the Linda interface are available.
 * The `eventRegister` primitive does not take a `Callback`, one is implicitly defined and tied to the execution itself.
 *
 * @see AbstractShell
 * @see Shell
 */
public class ShellWithFile extends AbstractShell {

    private final Linda linda;
    private final Callback shellCallback;

    private Scanner scanner;

    private int lineCounter;

    public ShellWithFile(File file) {
        this.linda = new CentralizedLinda();
        this.shellCallback = new ShellCallback();
        this.lineCounter = 0;

        try {
            this.scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        String line;
        String[] tokens;

        while (scanner.hasNextLine()) {
            line = scanner.nextLine();

            tokens = sanitize(line.split("\\s"));
            if (tokens.length > 0) {
                interpret(tokens);
            }
        }

        cleanExit(scanner, 0);
    }

    private void interpret(String[] tokens) {
        Method primitive = lindaPrimitives.get(tokens[0]);

        if (primitive == null) {
            System.out.printf(Locale.UK,
                    "linda: `%s`: primitive not found.\n",
                    tokens[0]);
            cleanExit(scanner, 1);
        }

        Object[] params = setParams(tokens);

        invokePrimitive(primitive, params);
    }

    private Object[] setParams(String[] tokens) {
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        List<Object> params = new ArrayList<>();

        switch (tokens[0]) {
            case "write":
            case "take":
            case "read":
            case "tryTake":
            case "tryRead":
            case "takeAll":
            case "readAll":
                String joinedString = String.join(" ", args);
                try {
                    params.add(Tuple.valueOf(joinedString));
                } catch (TupleFormatException ignored) {
                    System.out.printf(Locale.UK,
                            "`%s` is not a valid tuple.\n",
                            joinedString);
                    cleanExit(scanner, 1);
                }
                break;
            case "debug":
                params.add(args.length != 0 ? String.join(" ", args) : "(shell)");
                break;
            case "eventRegister":
                Tuple tuple = null;
                if (tokens[1].equalsIgnoreCase("read") || tokens[1].equalsIgnoreCase("take")) {
                    if (tokens[2].equalsIgnoreCase("immediate") || tokens[2].equalsIgnoreCase("future")) {
                        joinedString = String.join(" ", Arrays.copyOfRange(tokens, 3, tokens.length));
                        try {
                            tuple = Tuple.valueOf(joinedString);
                        } catch (TupleFormatException ignored) {
                            System.out.printf(Locale.UK,
                                    "`%s` is not a valid tuple.\n",
                                    joinedString);
                            cleanExit(scanner, 1);
                        }
                    }
                }
                if (tuple != null) {
                    params.add(Linda.eventMode.valueOf(tokens[1].toUpperCase()));
                    params.add(Linda.eventTiming.valueOf(tokens[2].toUpperCase()));
                    params.add(tuple);
                    params.add(shellCallback);
                }
                break;
            default:
                System.out.printf(Locale.UK,
                        "linda: `%s`: primitive not found.\n",
                        tokens[0]);
                cleanExit(scanner, 1);
        }
        return params.toArray();
    }

    private void invokePrimitive(Method primitive, Object[] params) {
        Object invocationResult = null;
        try {
            System.out.printf(Locale.UK,
                    "\t%%%%%% exec line %d, %s(%s).\n",
                    ++lineCounter,
                    primitive.getName(),
                    params.length != 0 ? Arrays.stream(params).map(Object::toString).collect(Collectors.joining(", ")) : "");
            invocationResult = primitive.invoke(linda, params);
        } catch (Exception e) {
            System.out.printf(Locale.UK,
                    "The primitive `%s` needs the following parameters: %s.\n",
                    primitive.getName(),
                    Arrays.toString(Arrays.stream(primitive.getParameterTypes()).map(Class::getSimpleName).toArray()));
            cleanExit(scanner, 1);
        }
        if (invocationResult != null) {
            System.out.println(invocationResult);
        }
    }
}
