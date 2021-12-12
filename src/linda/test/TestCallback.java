package linda.test;

import linda.Callback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;

public class TestCallback {

    private static Linda linda;

    public static void main(String[] a) {
        linda = new linda.shm.CentralizedLinda();

        Tuple temp1 = new Tuple(Integer.class, String.class);
        Tuple temp2 = new Tuple(String.class, String.class);

        Tuple t1 = new Tuple(-8, "bar");
        System.out.println("(1) write: " + t1);
        linda.write(t1);

        Tuple t2 = new Tuple(4, "foo");
        System.out.println("(1) write: " + t2);
        linda.write(t2);

        linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, temp1, new MyCallback("take immediate 1"));
        linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, temp2, new MyCallback("take immediate 2"));
        linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, temp1, new MyCallback("read immediate 1"));
        linda.eventRegister(eventMode.READ, eventTiming.FUTURE, temp1, new MyCallback("read future 1"));

        linda.debug("(main after CBs)");

        Tuple t3 = new Tuple(42, "baz");
        System.out.println("(1) write: " + t3);
        linda.write(t3);

        Tuple t4 = new Tuple("qux", "qux");
        System.out.println("(1) write: " + t4);
        linda.write(t4);

        linda.debug("(main end)");
    }

    private static class MyCallback implements Callback {
        private final String identifier;

        public MyCallback(String identifier) {
            this.identifier = identifier;
        }

        public void call(Tuple t) {
            System.out.println("CB " + identifier + " got " + t);
            linda.debug("(CB " + identifier + ")");
        }
    }
}
