package linda.test.full;

import linda.Linda;
import linda.Tuple;
import linda.util.TestUtils;

/**
 * As we write only 1 tuple in the tupleSpace and there are 1 take and 3 reads, there is no guarantee that all 3 reads
 * yield a result. The later the take is un-blocked, the more reads will yield a result. The sooner it is, the less
 * will. None will be if the take is the first one to take effect.
 * All reads that try returning (i.e. reading) a tuple after that 1 take will be blocked, as read, is, according to the
 * Linda spec, a blocking operation. The spec does not require any order while un-blocking the pending blocking
 * operations in this scenario.
 *
 * <p>Hence, running this {@link TestReadTake2} test might very well block, and indefinitely wait for new matching tuples
 * to un-block the rest of/all the reads.
 *
 * <p>Refer to {@link TestReadTake1} for a different scenario.
 */
public class TestReadTake2 {

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

        // write some matching tuples - 1 in this case
        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(4, 5);
            System.out.println("(2) write: " + t);
            linda.write(t);
        }).start();
    }
}
