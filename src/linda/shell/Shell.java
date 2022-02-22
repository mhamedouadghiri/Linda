package linda.shell;

import linda.Linda;
import linda.Tuple;
import linda.TupleFormatException;
import linda.server.CallbackRemote;
import linda.server.LindaRemote;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * A simple shell to interactively use with the Linda kernel.
 * <p>
 * All the common Linda primitives defined in the Linda interface are available.
 * Help is available in the shell by typing the `help` command.
 * The `eventRegister` primitive does not take a `Callback`, one is implicitly defined and tied to the shell session itself.
 *
 * @see AbstractShell
 * @see ShellWithFile
 */
public class Shell extends AbstractShell {

    private final Scanner scanner;
    private CallbackRemote shellCallback;
    private LindaRemote linda;
    private boolean exit;

    public Shell(String serverURI) {
        try {
            this.linda = (LindaRemote) Naming.lookup(serverURI);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.out.println("An error occurred when establishing connecting to the linda server.");
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
        try {
            this.shellCallback = new ShellCallback();
        } catch (RemoteException e) {
            System.out.println("An unexpected error has occurred.");
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
        this.scanner = new Scanner(System.in);
        this.exit = false;
    }

    @Override
    public void run() {
        String line;
        String[] tokens;

        while (!exit) {
            System.out.print("linda> ");
            line = scanner.nextLine();

            tokens = sanitize(line.split("\\s"));
            if (tokens.length > 0) {
                interpret(tokens);
            }
        }

        cleanExit(scanner, 0);
    }

    private void interpret(String[] tokens) {
        if (tokens[0].equals("exit")) {
            exit = true;
            return;
        }
        if (tokens[0].equals("help")) {
            System.out.println("Parameters description:\n" +
                    "\t<tuple>: A Tuple with a value represented by the specified String:\n" +
                    "\t\t Known values: integer, boolean, string, classname (eg. ?Integer), recursive tuple.\n" +
                    "\t\t Examples: [ 3 4 ], [ ?Integer \"foo\" true 42 ?Tuple ], [ [ false ?Object ] [ 3 [ \"bar\" ] 7 ] ]\n" +
                    "\t<string>: A string.\n" +
                    "\t<mode>: `read` or `take`.\n" +
                    "\t<timing>: `immediate` or `future`.\n" +
                    "These are all the available commands in the Linda shell:\n" +
                    "\twrite <tuple>: add the tuple to the tuplespace.\n" +
                    "\t\twrite [ 3 ?Tuple [ \"foo\" ?String ] false ] \n" +
                    "\ttake <tuple>: take a matching tuple, block if none is found.\n" +
                    "\t\ttake [ 3 true ] \n" +
                    "\tread <tuple>: read a matching tuple, block if none is found.\n" +
                    "\t\tread [ \"foo\" ?Integer ] \n" +
                    "\ttryTake <tuple>: take a matching tuple, return null if none is found.\n" +
                    "\t\ttryTake [ ?Object ?Object [ ] ] \n" +
                    "\ttryRead <tuple>: read a matching tuple, return null if none is found.\n" +
                    "\t\ttryRead [ [ ?Tuple ] ] \n" +
                    "\ttakeAll <tuple>: take all matching tuples, return empty is none is found.\n" +
                    "\t\ttakeAll [ ?Integer ?Number ] \n" +
                    "\treadAll <tuple>: take all matching tuples, return empty is none is found.\n" +
                    "\t\treadAll [ ?Integer false ] \n" +
                    "\teventRegister <mode> <timing> <tuple>: register an event, with an implicit Callback in this interactive shell.\n" +
                    "\t\teventRegister read immediate [ ?Boolean ?Integer ] \n" +
                    "\tdebug <string>: dump all existing tuples and callbacks with the specified prefix.\n" +
                    "\t\tdebug \"shell\" \n" +
                    "\thelp: show this message.\n" +
                    "\texit: terminate the shell and perform a clean exit.");
            return;
        }

        Method primitive = lindaPrimitives.get(tokens[0]);

        if (primitive == null) {
            System.out.printf(Locale.UK,
                    "linda: `%s`: command not found. Type `help` to display all available commands.\n",
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
                if (tokens.length < 4) {
                    System.out.printf(Locale.UK,
                            "The `eventRegister` primitive expects 3 parameters. Type `help` for more info.\n"
                    );
                    return null;
                }
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
                } else {
                    System.out.printf(Locale.UK,
                            "Wrong use of the `eventRegister` primitive. Type `help` for more info.\n"
                    );
                    return null;
                }
                break;
            default:
                System.out.printf(Locale.UK,
                        "linda: `%s`: command not found. Type `help` to display all available commands.\n",
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
            System.out.println("An error has occurred: " + e.getLocalizedMessage());
        }
    }
}
