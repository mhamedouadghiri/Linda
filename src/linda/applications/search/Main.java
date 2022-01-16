package linda.applications.search;

import linda.Linda;
import linda.shm.MultiThreadedCentralizedLinda;

public class Main {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("linda.applications.search.Main numberOfSearchers relativeFilePath searchQuery1 searchQuery2 searchQuery3 ...");
            return;
        }

        Linda linda = new MultiThreadedCentralizedLinda(8);

        for (int i = 2; i < args.length; i++) {
            Manager manager = new Manager(linda, args[1], args[i]);
            (new Thread(manager)).start();
        }

        for (int i = 0; i < Integer.parseInt(args[0]); i++) {
            Searcher searcher = new Searcher(linda);
            (new Thread(searcher)).start();
        }
    }
}
