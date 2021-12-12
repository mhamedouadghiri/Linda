package linda.test;

import linda.Linda;
import linda.Tuple;
import linda.util.TestUtils;

/***
 * As we write 3 tuples in the tupleSpace and there is only 1 take, we are sure that all 3 reads and 1 take will be
 * un-blocked. However, that might happen in any nondeterministic order, thus the 3 reads could potentially yield
 * different values (in case the take happens between one read and another).
 *
 * Refer to {@link TestReadTake2} for a different scenario.
 */
public class TestReadTake1 {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();

        // read 1
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res = linda.read(template);
            System.out.println("(1.1 read) Result:" + res);
        }).start();

        // read 2
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res = linda.read(template);
            System.out.println("(1.2 read) Result:" + res);
        }).start();

        // read 3
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res = linda.read(template);
            System.out.println("(1.3 read) Result:" + res);
        }).start();

        // take
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple result = linda.take(template);
            System.out.println("(1.1 take) Result:" + result);
        }).start();

        // write some matching tuples
        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(4, 5);
            System.out.println("(2) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(-4, Integer.class);
            System.out.println("(3) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(Integer.class, -42);
            System.out.println("(4) write: " + t);
            linda.write(t);
        }).start();
    }
}
