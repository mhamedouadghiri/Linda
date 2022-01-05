package linda.test.full;

import linda.Callback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;
import linda.server.LindaClient;
import linda.util.TestUtils;

import java.util.Collection;

public class TestDecentralized {

    // two clients
    private static Linda linda;
    private static Linda linda2;

    public static void main(String[] a) {
        linda = new LindaClient("//localhost:7878/server");
        linda2 = new LindaClient("//localhost:7878/server");

        // writes from both linda and linda2
        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(4, 5);
            System.out.println("(1) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(42, "foobar");
            System.out.println("(2) write: " + t);
            linda2.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(1337, Integer.class);
            System.out.println("(3) write: " + t);
            linda2.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(20);

            Tuple t = new Tuple(1, Integer.class, String.class);
            System.out.println("(4) write: " + t);
            linda2.write(t);

            t = new Tuple(1, 42, "foo");
            System.out.println("(5) write: " + t);
            linda.write(t);

            t = new Tuple(1, Integer.class, "foo");
            System.out.println("(6) write: " + t);
            linda.write(t);

            t = new Tuple(1, 0, "foo".getClass());
            System.out.println("(7) write: " + t);
            linda.write(t);
        }).start();

        // read
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, String.class);
            Tuple res = linda.read(template);
            System.out.println("(read 1) Result:" + res);
            linda.debug("(read 1)");
        }).start();

        // readAll
        new Thread(() -> {
            TestUtils.sleep(500);  // readAll is non-blocking, wait for some tuples to be written
            Tuple template = new Tuple(Integer.class, Integer.class);
            Collection<Tuple> res = linda2.readAll(template);
            System.out.println("(readAll 2) Result:" + res);
            linda2.debug("(readAll 2)");
        }).start();

        // take
        new Thread(() -> {
            Tuple template = new Tuple(Integer.class, Integer.class, String.class);
            Tuple result = linda2.take(template);
            System.out.println("(take 3) Result:" + result);
            linda2.debug("(take 3)");
        }).start();

        // takeAll
        new Thread(() -> {
            TestUtils.sleep(500);  // takeAll is non-blocking, wait for some tuples to be written, and for take
            Tuple template = new Tuple(Integer.class, Integer.class, String.class);
            Collection<Tuple> res = linda.takeAll(template);
            System.out.println("(takeAll 4) Result: " + res);
            linda.debug("(takeAll 4)");
        }).start();

        // callbacks
        new Thread(() -> {
            TestUtils.sleep(400);
            Tuple template = new Tuple(Integer.class, String.class);
            linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, template, new MyCallback("read immediate 5"));
            linda2.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template, new MyCallback("take future 6"));
            linda2.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, template, new MyCallback("take immediate 7"));
            linda.eventRegister(eventMode.READ, eventTiming.FUTURE, template, new MyCallback("read future 8"));

            TestUtils.sleep(200);

            Tuple t = new Tuple(1970, "Epoch");
            System.out.println("(8) write: " + t);
            linda2.write(t);

            t = new Tuple(2038, "Y2K38");
            System.out.println("(9) write: " + t);
            linda.write(t);
        }).start();

        new Thread(() -> {
            TestUtils.sleep(2000);
            linda.debug("(linda)");
        }).start();
    }

    private static class MyCallback implements Callback {
        private final String identifier;

        public MyCallback(String identifier) {
            this.identifier = identifier;
        }

        public void call(Tuple t) {
            System.out.println("CB " + identifier + " got " + t);
        }
    }
}
