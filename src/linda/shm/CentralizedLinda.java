package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Shared memory implementation of Linda.
 */
public class CentralizedLinda implements Linda {

    protected final Lock lock;
    private final List<Tuple> tuples;
    private final List<EventCallback> eventCallbacks;

    // use of the thread-safe BlockingQueue (and a LinkedBlockingQueue as its implementation)
    private final Map<Tuple, BlockingQueue<Condition>> reads;
    private final Map<Tuple, BlockingQueue<Condition>> takes;

    public CentralizedLinda() {
        tuples = new ArrayList<>();
        lock = new ReentrantLock();
        eventCallbacks = new ArrayList<>();
        reads = new HashMap<>();
        takes = new HashMap<>();
    }

    @Override
    public void write(Tuple t) {
        if (t == null) {
            System.err.println("LOG (w): Attempted to persist a null tuple, operation aborted.");
            return;
        }
        lock.lock();
        tuples.add(t.deepclone());
        checkCallbacks(t);
        signal(t);
        lock.unlock();
    }

    @Override
    public Tuple take(Tuple template) {
        if (template == null) {
            return null;
        }
        lock.lock();
        Tuple hit;
        Condition condition;
        while ((hit = tryTake(template)) == null) {
            condition = lock.newCondition();
            takes.putIfAbsent(template, new LinkedBlockingQueue<>());
            takes.get(template).add(condition);
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock.unlock();
        return hit;
    }

    @Override
    public Tuple read(Tuple template) {
        if (template == null) {
            return null;
        }
        lock.lock();
        Tuple hit;
        Condition condition;
        while ((hit = tryRead(template)) == null) {
            condition = lock.newCondition();
            reads.putIfAbsent(template, new LinkedBlockingQueue<>());
            reads.get(template).add(condition);
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock.unlock();
        return hit;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        lock.lock();
        Tuple hit = tryRead(template);
        if (hit != null) {
            tuples.remove(hit);
        }
        lock.unlock();
        return hit;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        if (template == null) {
            return null;
        }
        lock.lock();
        Tuple hit = null;
        for (Tuple tuple : tuples) {
            if (tuple.matches(template)) {
                hit = tuple.deepclone();
                // the spec does not specify which one to get, so we return the first one encountered
                break;
            }
        }
        lock.unlock();
        return hit;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        lock.lock();
        Collection<Tuple> hits = readAll(template);
        hits.forEach(tuples::remove);
        lock.unlock();
        return hits;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        if (template == null) {
            return new ArrayList<>();
        }
        lock.lock();
        Collection<Tuple> hits = new ArrayList<>();
        for (Tuple tuple : tuples) {
            if (tuple.matches(template)) {
                hits.add(tuple);
            }
        }
        lock.unlock();
        return hits;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        // first, we create an EventCallback and register it
        EventCallback eventCallback = new EventCallback(mode, template, callback);
        eventCallbacks.add(eventCallback);

        // then, if the timing is IMMEDIATE, we'll search for a match and remove the callback in case it is found
        if (timing == eventTiming.IMMEDIATE) {
            lock.lock();

            Tuple hit = null;
            if (mode == eventMode.READ) {
                hit = tryRead(template);
            } else if (mode == eventMode.TAKE) {
                hit = tryTake(template);
            }

            if (hit != null) {
                eventCallbacks.remove(eventCallback);
                callback.call(hit);
            }

            lock.unlock();
        }
    }

    @Override
    public void debug(String prefix) {
        lock.lock();
        System.out.println();
        debug("TupleSpace", prefix, tuples);
        debug("Callbacks", prefix, eventCallbacks);
        System.out.println();
        lock.unlock();
    }

    private void debug(String identifier, String prefix, Collection<?> collection) {
        lock.lock();
        if (collection.isEmpty()) {
            System.out.format(" --- %s collection with prefix %s is empty. --- \n", identifier, prefix);
            lock.unlock();
            return;
        }
        System.out.format(" *** Start %s dump with prefix %s. *** \n", identifier, prefix);
        collection.forEach(System.out::println);
        System.out.format(" *** End %s dump with prefix %s. *** \n", identifier, prefix);
        lock.unlock();
    }

    protected void checkCallbacks(Tuple tuple) {
        lock.lock();
        List<EventCallback> collect = eventCallbacks.stream()
                .filter(eventCallback -> tuple.matches(eventCallback.template))
                .collect(Collectors.toList());
        for (EventCallback eventCallback : collect) {
            if (eventCallback.mode == eventMode.TAKE) {
                tryTake(tuple);
            }
            eventCallbacks.remove(eventCallback);
            eventCallback.callback.call(tuple);
        }
        lock.unlock();
    }

    /**
     * Signal/wake all desired waiting operations (i.e. read/take) pertaining to a particular template.
     *
     * @param tuple the newly written tuple acting as a template
     */
    protected void signal(Tuple tuple) {
        lock.lock();
        Condition condition;
        for (Tuple template : reads.keySet()) {
            if (tuple.matches(template)) {
                while ((condition = reads.get(template).poll()) != null) {
                    condition.signal();
                }
            }
        }
        boolean signaled = false;
        for (Tuple template : takes.keySet()) {
            if (!signaled) {
                if (tuple.matches(template)) {
                    if ((condition = takes.get(template).poll()) != null) {
                        condition.signal();
                        signaled = true;
                    }
                }
            }
        }
        lock.unlock();
    }

    private static class EventCallback {
        private final eventMode mode;
        private final Tuple template;
        private final Callback callback;

        public EventCallback(eventMode mode, Tuple template, Callback callback) {
            this.mode = mode;
            this.template = template;
            this.callback = callback;
        }

        @Override
        public String toString() {
            return "EventCallback{" +
                    "mode=" + mode +
                    ", template=" + template +
                    ", callback=" + callback +
                    '}';
        }
    }
}
