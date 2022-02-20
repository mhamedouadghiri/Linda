package linda.shell;

import java.io.File;

public class RunShell {

    public static void main(String[] args) {
        if (args.length == 0) {
            new Shell().run();
        } else if (args.length == 1) {
            new ShellWithFile(new File(args[0])).run();
        } else {
            System.out.println("For the interactive mode, launch without args.\n" +
                    "For the script mode, launch with one arg (filename of the script).");
            System.exit(1);
        }
    }
}
