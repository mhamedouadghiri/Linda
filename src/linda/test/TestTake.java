package linda.test;

import linda.Linda;
import linda.Tuple;
import linda.util.TestUtils;

public class TestTake {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();

        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple result = linda.take(template);
            System.out.println("(1.1) Result:" + result);
            linda.debug("(1.1)");
        }).start();

        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple result = linda.take(template);
            System.out.println("(1.2) Result:" + result);
            linda.debug("(1.2)");
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

        // empty
        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple();
            System.out.println("(3) write: " + t);
            linda.write(t);
        }).start();

        // null
        new Thread(() -> {
            linda.take(null);
        }).start();
    }
}
