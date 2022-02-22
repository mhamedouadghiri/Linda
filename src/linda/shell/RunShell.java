package linda.shell;

import java.io.File;

public class RunShell {

    public static void main(String[] args) {
        if (args.length == 1) {
            new Shell(args[0]).run();
        } else if (args.length == 2) {
            new ShellWithFile(args[0], new File(args[1])).run();
        } else {
            System.out.println("For the interactive mode, launch with 1 arg: address of the linda server.\n" +
                    "\t Example: java RunShell rmi://localhost:7878/server \n" +
                    "For the script mode, launch with 2 args: address of the linda server and filename of the script." +
                    "\t Example: java RunShell rmi://localhost:7878/server scenario\n"
            );
            System.exit(1);
        }
    }
}
