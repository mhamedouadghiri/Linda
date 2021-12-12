package linda.test.full;

import linda.Linda;
import linda.Tuple;
import linda.util.TestUtils;

public class TestTryTake {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();

        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res = linda.tryTake(template);
            System.out.println("(1.1) Result:" + res);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(100);

            linda.debug("(1.2 before)");
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res = linda.tryTake(template);
            System.out.println("(1.2) Result:" + res);
            linda.debug("(1.2 after)");
        }).start();

        new Thread(() -> {
//            TestUtils.sleep(20);

            Tuple t = new Tuple(4, 5);
            System.out.println("(2) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
//            TestUtils.sleep(20);

            Tuple t = new Tuple(-8, Integer.class);
            System.out.println("(3) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
//            TestUtils.sleep(20);

            Tuple t = new Tuple(0, -1);
            System.out.println("(3) write: " + t);
            linda.write(t);
        }).start();

        // null
        new Thread(() -> {
            linda.tryTake(null);
        }).start();
    }
}
