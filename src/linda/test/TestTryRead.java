package linda.test;

import linda.Linda;
import linda.Tuple;
import linda.util.TestUtils;

public class TestTryRead {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();

        // try take
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res1 = linda.tryRead(template);
            System.out.println("(1.1) Result:" + res1);
            linda.debug("(1.1)");
        }).start();

        // try take after 100 millis (after write)
        new Thread(() -> {
            TestUtils.sleep(100);

            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res1 = linda.tryRead(template);
            System.out.println("(1.2) Result:" + res1);
            linda.debug("(1.2)");
        }).start();

        // mixed template
        new Thread(() -> {
            TestUtils.sleep(100);

            Tuple template = new Tuple(4, Integer.class);
            Tuple res1 = linda.tryRead(template);
            System.out.println("(1.3) Result:" + res1);
            linda.debug("(1.3)");
        }).start();

        // other template
        new Thread(() -> {
            TestUtils.sleep(100);

            Tuple template = new Tuple(Object.class, Number.class);
            Tuple res1 = linda.tryRead(template);
            System.out.println("(1.4) Result:" + res1);
            linda.debug("(1.4)");
        }).start();

        // write
        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t1 = new Tuple(4, 5);
            System.out.println("(2) write: " + t1);
            linda.write(t1);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t2 = new Tuple(4, Integer.class);
            System.out.println("(2) write: " + t2);
            linda.write(t2);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t3 = new Tuple(Integer.class, "foobar");
            System.out.println("(2) write: " + t3);
            linda.write(t3);
        }).start();

        // empty
        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t2 = new Tuple();
            System.out.println("(3) write: " + t2);
            linda.write(t2);

            linda.debug("(3)");
        }).start();

        // null
        new Thread(() -> {
            linda.tryRead(null);
            linda.debug("(null)");
        }).start();
    }
}
