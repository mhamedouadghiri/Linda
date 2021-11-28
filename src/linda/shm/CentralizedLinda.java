package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.Collection;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    public CentralizedLinda() {
    }

    @Override
    public void write(Tuple t) {

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
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        return null;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        return null;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {

    }

    @Override
    public void debug(String prefix) {

    }

    // TO BE COMPLETED

}
