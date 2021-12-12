package linda.test;

import linda.Callback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;
import linda.util.TestUtils;

/***
 * Running this {@link TestCallbackTake} test should always block, indefinitely waiting for a matching tuple to take;
 * as the "write primitive" always checks the callbacks before waking up any other "normal" read/take.
 */
public class TestCallbackTake {

    private static Linda linda;

    public static void main(String[] a) {
        linda = new linda.shm.CentralizedLinda();

        Tuple temp = new Tuple(Integer.class, String.class);

        // this should always block
        new Thread(() -> {
            Tuple result = linda.take(temp);
            System.out.println("(take) Result:" + result);
        }).start();

        // this should always be called
        new Thread(() -> {
            TestUtils.sleep(20);
            linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, temp, new MyCallback("take future"));
        }).start();

        new Thread(() -> {
            TestUtils.sleep(150);
            Tuple t = new Tuple(-8, "bar");
            System.out.println("(1) write: " + t);
            linda.write(t);
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
