package linda.shell;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.TupleFormatException;
import linda.shm.CentralizedLinda;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Shell {

    private static final Map<String, Method> lindaPrimitives = getLindaPrimitives();

    private final Linda linda;
    private final Callback shellCallback;

    private boolean exit;

    public Shell() {
        this.linda = new CentralizedLinda();
        this.shellCallback = new ShellCallback();
        this.exit = false;
    }

    private static Map<String, Method> getLindaPrimitives() {
        return Arrays.stream(Linda.class.getMethods())
                .collect(Collectors.toMap(Method::getName, Function.identity()));
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        String line;
        String[] tokens;

        while (!exit) {
            System.out.print("linda > ");
            line = scanner.nextLine();

            tokens = sanitize(line.split("\\s"));
            if (tokens.length > 0) {
                interpret(tokens);
            }
        }

        System.exit(0);
    }

    private void interpret(String[] tokens) {
        if (tokens[0].equals("exit")) {
            exit = true;
            return;
        }
        if (tokens[0].equals("help")) {
            System.out.println("Help message in progress...");
            return;
        }

        Method primitive = lindaPrimitives.get(tokens[0]);

        if (primitive == null) {
            System.out.printf(Locale.UK,
                    "linda: `%s`: primitive not found. Type `help` to display all available commands.\n",
                    tokens[0]);
            return;
        }

        Object[] params = setParams(tokens);

        if (params == null) {
            return;
        }

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
                    return null;
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
                            return null;
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
                        "linda: `%s`: primitive not found. Type `help` to display all available commands.\n",
                        tokens[0]);
                return null;
        }
        return params.toArray();
    }

    private void invokePrimitive(Method primitive, Object[] params) {
        try {
            Object invocationResult = primitive.invoke(linda, params);
            if (invocationResult != null) {
                System.out.println(invocationResult);
            }
        } catch (Exception e) {
            System.out.printf(Locale.UK,
                    "The primitive `%s` needs the following parameters: %s.\n",
                    primitive.getName(),
                    Arrays.toString(Arrays.stream(primitive.getParameterTypes()).map(Class::getSimpleName).toArray()));
        }
    }

    private String[] sanitize(String[] strings) {
        return Arrays.stream(strings)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private static class ShellCallback implements Callback {
        public void call(Tuple t) {
            System.out.printf(Locale.UK, "Callback triggered, got `%s`.\n", t);
        }
    }
}
