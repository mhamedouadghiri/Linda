package linda.test;

import linda.Linda;
import linda.Tuple;
import linda.util.TestUtils;

public class TestRead {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();

        // read tuple... will block until one is written
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res = linda.read(template);
            System.out.println("(1.1) Result:" + res);
            linda.debug("(1.1)");
        }).start();

        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res = linda.read(template);
            System.out.println("(1.2) Result:" + res);
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

        // empty template
        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple empty = new Tuple();
            System.out.println("(5) write: " + empty);
            linda.write(empty);
        }).start();

        // null template
        new Thread(() -> {
            linda.read(null);
        }).start();
    }
}
