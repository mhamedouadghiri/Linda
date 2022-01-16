package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MultiThreadedCentralizedLinda extends CentralizedLinda {

    private static final int DEFAULT_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

    private final List<Linda> lindas = new ArrayList<>();

    public MultiThreadedCentralizedLinda() {
        this(DEFAULT_NUMBER_OF_THREADS);
    }

    public MultiThreadedCentralizedLinda(int numberOfThreads) {
        for (int i = 0; i < numberOfThreads; i++) {
            lindas.add(new CentralizedLinda());
        }
    }

    @Override
    public void write(Tuple t) {
        if (t == null) {
            System.err.println("LOG (w): Attempted to persist a null tuple, operation aborted.");
            return;
        }
        lock.lock();
        lindas.get(ThreadLocalRandom.current().nextInt(lindas.size())).write(t);
        lock.unlock();
    }

    @Override
    public Tuple take(Tuple template) {
        return super.take(template);
    }

    @Override
    public Tuple read(Tuple template) {
        return super.read(template);
    }

    @Override
    public Tuple tryTake(Tuple template) {
        lock.lock();
        Tuple hit = null;
        for (Linda linda : lindas) {
            if ((hit = linda.tryTake(template)) != null) {
                break;
            }
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
        for (Linda linda : lindas) {
            if ((hit = linda.tryRead(template)) != null) {
                break;
            }
        }
        lock.unlock();
        return hit;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        lock.lock();
        Collection<Tuple> hits = lindas.stream()
                .flatMap(linda -> linda.takeAll(template).stream())
                .collect(Collectors.toList());
        lock.unlock();
        return hits;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        if (template == null) {
            return new ArrayList<>();
        }
        lock.lock();
        Collection<Tuple> hits = lindas.stream().
                flatMap(linda -> linda.readAll(template).stream())
                .collect(Collectors.toList());
        lock.unlock();
        return hits;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        super.eventRegister(mode, timing, template, callback);
    }

    @Override
    public void debug(String prefix) {
        lock.lock();
        lindas.forEach(linda -> linda.debug(prefix));
        lock.unlock();
    }
}
