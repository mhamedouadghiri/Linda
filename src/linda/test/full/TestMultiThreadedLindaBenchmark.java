package linda.test.full;

import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;
import linda.shm.MultiThreadedCentralizedLinda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class TestMultiThreadedLindaBenchmark {
    public static void main(String[] args) {
        int n = 10000;
        int iterations = 3;
        System.out.printf("Launching %d iteration(s) on %d tuples\n", iterations, n);
        benchmark(n, iterations);
    }

    private static Collection<Tuple> normalWriteRead(int n) {
        Linda linda = new CentralizedLinda();

        IntStream.rangeClosed(1, n).mapToObj(i -> new Tuple(i)).forEach(linda::write);

        return linda.readAll(new Tuple(Integer.class));
    }

    private static Collection<Tuple> threadedWriteRead(int n, int numberThreads) {
        Linda linda = new MultiThreadedCentralizedLinda(numberThreads);

        IntStream.rangeClosed(1, n).mapToObj(i -> new Tuple(i)).forEach(linda::write);

        return linda.readAll(new Tuple(Integer.class));
    }

    private static Collection<Tuple> normalWriteTake(int n) {
        Linda linda = new CentralizedLinda();

        IntStream.rangeClosed(1, n).mapToObj(i -> new Tuple(i)).forEach(linda::write);

        return linda.takeAll(new Tuple(Integer.class));
    }

    private static Collection<Tuple> threadedWriteTake(int n, int numberThreads) {
        Linda linda = new MultiThreadedCentralizedLinda(numberThreads);

        IntStream.rangeClosed(1, n).mapToObj(i -> new Tuple(i)).forEach(linda::write);

        return linda.takeAll(new Tuple(Integer.class));
    }

    private static void benchmark(int n, int iterations) {
        double time;
        List<Long> longs = new ArrayList<>();
        Collection<Tuple> normal = null;
        Collection<Tuple> threaded;

        System.out.println("******** Write/Read benchmark ********");

        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            normal = normalWriteRead(n);
            long end = System.currentTimeMillis();
            longs.add(end - start);
        }
        time = longs.stream().mapToLong(value -> value).average().orElse(Double.NaN) / 1_000;
        printSummary("Normal linda write/read", time, -1);

        for (int numberThreads = 2; numberThreads <= Runtime.getRuntime().availableProcessors(); numberThreads+=2) {
            longs = new ArrayList<>();
            for (int i = 0; i < iterations; i++) {
                long start = System.currentTimeMillis();
                threaded = threadedWriteRead(n, numberThreads);
                long end = System.currentTimeMillis();
                longs.add(end - start);

                // sanity check
                if (!normal.containsAll(threaded) || !threaded.containsAll(normal)) {
                    throw new RuntimeException("An error has occurred.");
                }
            }
            time = longs.stream().mapToLong(value -> value).average().orElse(Double.NaN) / 1_000;
            printSummary("Multi-threaded linda write/read", time, numberThreads);
        }

        System.out.println("******** Write/Take benchmark ********");

        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            normal = normalWriteTake(n);
            long end = System.currentTimeMillis();
            longs.add(end - start);
        }
        time = longs.stream().mapToLong(value -> value).average().orElse(Double.NaN) / 1_000;
        printSummary("Normal linda write/take", time, -1);

        for (int numberThreads = 2; numberThreads <= Runtime.getRuntime().availableProcessors(); numberThreads+=2) {
            longs = new ArrayList<>();
            for (int i = 0; i < iterations; i++) {
                long start = System.currentTimeMillis();
                threaded = threadedWriteTake(n, numberThreads);
                long end = System.currentTimeMillis();
                longs.add(end - start);

                // sanity check
                if (!normal.containsAll(threaded) || !threaded.containsAll(normal)) {
                    throw new RuntimeException("An error has occurred.");
                }
            }
            time = longs.stream().mapToLong(value -> value).average().orElse(Double.NaN) / 1_000;
            printSummary("Multi-threaded linda write/take", time, numberThreads);
        }
    }

    private static void printSummary(String type, double time, int numberThreads) {
        System.out.println(type + ": ");
        if (numberThreads > 0) {
            System.out.printf("\t%d threads\n", numberThreads);
        }
        System.out.printf("\ttime: %.5f sec\n", time);
    }
}
