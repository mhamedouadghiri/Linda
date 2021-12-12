package linda.test.full;

import linda.Callback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;

/**
 * Will enter an infinite loop, raise a StackOverflowError exception then crash!
 */
public class TestCallbackLoop {

    private static Linda linda;
    private static Tuple temp;

    public static void main(String[] a) {
        linda = new linda.shm.CentralizedLinda();

        temp = new Tuple(Integer.class, String.class);

        Tuple t = new Tuple(42, "foobar");
        linda.write(t);

        linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, temp, new MyCallback());
    }

    private static class MyCallback implements Callback {
        public void call(Tuple t) {
            System.out.println("CB got " + t);
            linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, temp, this);
        }
    }
}
