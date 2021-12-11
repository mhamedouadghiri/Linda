package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared memory implementation of Linda.
 */
public class CentralizedLinda implements Linda {

    private final List<Tuple> tuples = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final List<EventCallback> eventCallbacks = new ArrayList<>();

    public CentralizedLinda() {
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
        condition.signalAll();
        lock.unlock();
    }

    @Override
    public Tuple take(Tuple template) {
        lock.lock();
        Tuple hit;
        while ((hit = tryTake(template)) == null) {
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
        while ((hit = tryRead(template)) == null) {
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
        lock.lock();
        Tuple hit = null;
        if (timing == eventTiming.IMMEDIATE) {
            if (mode == eventMode.READ) {
                hit = tryRead(template);
            } else if (mode == eventMode.TAKE) {
                hit = tryTake(template);
            }
        }

        if (hit != null) {
            callback.call(hit);
        } else {
            // either the timing is FUTURE, or it is IMMEDIATE but no match was currently found
            // in either case we register the callback for future use
            eventCallbacks.add(new EventCallback(mode, template, callback));
        }

        lock.unlock();
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
            lock.unlock();
            return;
        }
        System.out.format("\n *** Start %s dump with prefix %s. *** \n", identifier, prefix);
        collection.forEach(System.out::println);
        System.out.format(" *** End %s dump with prefix %s. *** \n", identifier, prefix);
        lock.unlock();
    }

    private void checkCallbacks(Tuple template) {
        lock.lock();
        List<EventCallback> toRemove = new ArrayList<>();
        for (EventCallback eventCallback : eventCallbacks) {
            if (eventCallback.tryOperation(template) != null) {
                toRemove.add(eventCallback);
                if (eventCallback.mode == eventMode.TAKE) {
                    break;
                }
            }
        }
        eventCallbacks.removeAll(toRemove);
        lock.unlock();
    }

    private class EventCallback {
        private final eventMode mode;
        private final Tuple template;
        private final Callback callback;

        public EventCallback(eventMode mode, Tuple template, Callback callback) {
            this.mode = mode;
            this.template = template;
            this.callback = callback;
        }

        public Tuple tryOperation(Tuple tupleToFind) {
            if (!tupleToFind.matches(template)) {
                return null;
            }
            Tuple hit = null;
            if (mode == eventMode.READ) {
                hit = tryRead(tupleToFind);
            } else if (mode == eventMode.TAKE) {
                hit = tryTake(tupleToFind);
            }

            if (hit != null) {
                callback.call(hit);
            }
            return hit;
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
