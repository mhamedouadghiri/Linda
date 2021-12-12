package linda.test;

import linda.Linda;
import linda.Tuple;
import linda.util.TestUtils;

import java.io.Serializable;
import java.util.Collection;

public class TestTakeAll {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();

        new Thread(() -> {
            Tuple template = new Tuple(Object.class, String.class);
            Collection<Tuple> res = linda.takeAll(template);
            System.out.println("(take 1) Result: " + res);
            linda.debug("(take 1)");
        }).start();

        // wait for some tuples to be written
        new Thread(() -> {
            TestUtils.sleep(300);

            Tuple template = new Tuple(Object.class, String.class);
            Collection<Tuple> res = linda.takeAll(template);
            System.out.println("(take 2) Result: " + res);
            linda.debug("(take 2)");
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(4, "");
            System.out.println("(1) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple("foo", "bar");
            System.out.println("(2) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple("foobar", String.class);
            System.out.println("(3) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(Foobar.class, String.class);
            System.out.println("(4) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(new Tuple(Foobar.class, Tuple.class), String.class);
            System.out.println("(5) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            Collection<Tuple> res = linda.takeAll(null);
            System.out.println("(take null) Result: " + res);
            linda.debug("(take null)");
        }).start();
    }

    private static class Foobar implements Serializable {
    }
}
