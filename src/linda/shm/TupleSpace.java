package linda.shm;

import linda.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class TupleSpace implements Collection<Tuple> {

    public static final int NUMBER_THREADS = Runtime.getRuntime().availableProcessors();

    private final List<List<Tuple>> tuples;

    public TupleSpace() {
        tuples = new ArrayList<>();
        for (int i = 0; i < NUMBER_THREADS; i++) {
            tuples.add(new ArrayList<>());
        }
    }

    @Override
    public int size() {
        return tuples.stream().mapToInt(List::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        throw new OperationNotImplementedException();
    }

    @Override
    public Iterator<Tuple> iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        throw new OperationNotImplementedException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new OperationNotImplementedException();
    }

    @Override
    public boolean add(Tuple serializables) {
        return tuples.get(ThreadLocalRandom.current().nextInt(NUMBER_THREADS)).add(serializables);
    }

    @Override
    public boolean remove(Object o) {
        for (List<Tuple> tupleList : tuples) {
            if (tupleList.contains(((Tuple) o))) {
                return tuples.remove(o);
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new OperationNotImplementedException();
    }

    @Override
    public boolean addAll(Collection<? extends Tuple> c) {
        throw new OperationNotImplementedException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new OperationNotImplementedException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new OperationNotImplementedException();
    }

    @Override
    public void clear() {
        throw new OperationNotImplementedException();
    }

    public Tuple find(Tuple template) {
        return tuples.parallelStream()
                .flatMap(Collection::stream)
                .filter(tuple -> tuple.matches(template))
                .findFirst()
                .orElse(null);
    }

    private static class OperationNotImplementedException extends RuntimeException {
        public OperationNotImplementedException() {
            System.out.println("Operation not implemented.");
        }

        public OperationNotImplementedException(String name) {
            System.out.println(name + " is not implemented.");
        }
    }

    private class Itr implements Iterator<Tuple> {
        private int cursor = 0;  // a global cursor for the TupleSpace, index of the next element to return
        private int currentTupleList = 0;  // the current tuple list (element of the TupleSpace)
        private int currentTupleListCursor = 0;  // index, in the current tuple list, of the next element to return

        @Override
        public boolean hasNext() {
            return cursor < size();
        }

        @Override
        public Tuple next() {
            checkAndSkipEmpty();
            cursor++;
            return tuples.get(currentTupleList).get(currentTupleListCursor++);
        }

        @Override
        public void remove() {
            checkAndSkipEmpty();
            cursor++;
            tuples.get(currentTupleList).remove(currentTupleListCursor++);
        }

        @Override
        public void forEachRemaining(Consumer<? super Tuple> action) {
            Iterator.super.forEachRemaining(action);
        }

        private void checkAndSkipEmpty() {
            // while we are not finished with the current tuple list
            while (currentTupleListCursor == tuples.get(currentTupleList).size()) {
                currentTupleList++;  // move to the next one
                currentTupleListCursor = 0;  // and reset its cursor
            }
        }
    }
}
