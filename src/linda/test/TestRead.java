package linda.test;

import linda.Linda;
import linda.Tuple;

public class TestRead {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();

        // read integer tuple... will block until one is written
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class);
            Tuple res = linda.read(template);
            System.out.println("(1) Result:" + res);
            linda.debug("(1)");
        }).start();

        // write integer tuple
        new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Tuple t1 = new Tuple(4, 5);
            System.out.println("(2) write: " + t1);
            linda.write(t1);

            linda.debug("(2)");
        }).start();

        // empty template
        new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Tuple empty = new Tuple();
            System.out.println("(3) write: " + empty);
            linda.write(empty);

            linda.debug("(empty)");
        }).start();

        // null template
        new Thread(() -> {
            linda.read(null);
            linda.debug("(null)");
        }).start();
    }
}
