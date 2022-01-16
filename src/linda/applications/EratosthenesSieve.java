package linda.applications;

import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;
import linda.shm.MultiThreadedCentralizedLinda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EratosthenesSieve {
    public static void main(String[] args) {
        int n = 10_000;
        int iterations = 10;
        System.out.printf("Launching %d iteration of Eratosthenes for primes up to %d\n", iterations, n);
        benchmark(n, iterations);
    }

    private static List<Integer> normalSieve(int n) {
        boolean[] booleans = new boolean[n];
        Arrays.fill(booleans, true);

        for (int i = 2; i < Math.sqrt(n); i++) {
            if (booleans[i]) {
                for (int j = i * i; j < n; j += i) {
                    booleans[j] = false;
                }
            }
        }

        return IntStream.range(2, booleans.length).filter(i -> booleans[i]).boxed().collect(Collectors.toList());
    }

    private static List<Integer> lindaSieve(int n) {
        Linda linda = new CentralizedLinda();

        List<Integer> primes = new ArrayList<>();

        IntStream.rangeClosed(2, n).mapToObj(i -> new Tuple(i)).forEach(linda::write);

        for (int i = 2; i < n + 1; i++) {
            Tuple tuple = linda.tryRead(new Tuple(i));
            if (tuple != null) {
                int prime = (Integer) tuple.element();
                primes.add(prime);
                for (int j = prime * prime; j < n + 1; j += prime) {
                    linda.tryTake(new Tuple(j));
                }
            }
        }

        return primes;
    }

    private static List<Integer> threadedLindaSieve(int n) {
        // 8 seems like the sweet spot in a machine with 12 logical processors
        Linda linda = new MultiThreadedCentralizedLinda(8);

        List<Integer> primes = new ArrayList<>();

        IntStream.rangeClosed(2, n).mapToObj(i -> new Tuple(i)).forEach(linda::write);

        for (int i = 2; i < n + 1; i++) {
            Tuple tuple = linda.tryRead(new Tuple(i));
            if (tuple != null) {
                int prime = (Integer) tuple.element();
                primes.add(prime);
                for (int j = prime * prime; j < n + 1; j += prime) {
                    linda.tryTake(new Tuple(j));
                }
            }
        }

        return primes;
    }

    private static void benchmark(int n, int iterations) {
        double time;
        int size = -1;

        List<Long> longs = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            size = normalSieve(n).size();
            long end = System.currentTimeMillis();
            longs.add(end - start);
        }
        time = longs.stream().mapToLong(value -> value).average().orElse(Double.NaN) / 1_000;
        printSummary("Normal Sieve", size, time);

        longs = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            size = lindaSieve(n).size();
            long end = System.currentTimeMillis();
            longs.add(end - start);
        }
        time = longs.stream().mapToLong(value -> value).average().orElse(Double.NaN) / 1_000;
        printSummary("Linda Sieve", size, time);

        longs = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            size = threadedLindaSieve(n).size();
            long end = System.currentTimeMillis();
            longs.add(end - start);
        }
        time = longs.stream().mapToLong(value -> value).average().orElse(Double.NaN) / 1_000;
        printSummary("Multi-threaded Linda Sieve", size, time);
    }

    private static void printSummary(String type, int size, double time) {
        System.out.println(type + ": ");
        System.out.println("\tnb of primes: " + size);
        System.out.printf("\ttime: %.5f sec\n", time);
    }
}
