package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared memory implementation of Linda.
 */
public class CentralizedLinda implements Linda {

    private final List<Tuple> tuples = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

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
        lock.unlock();
    }

    @Override
    public Tuple take(Tuple template) {
        return null;
    }

    @Override
    public Tuple read(Tuple template) {
        return null;
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

    }

    @Override
    public void debug(String prefix) {
        tuples.forEach(t -> System.out.println(prefix + t));
    }
}
