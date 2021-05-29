package com.thordickinson.searchworkers.util;
import java.util.LinkedList;

/**
 * A queue with a limited size that will remove the first element everytime it reaches
 * the size limit.
 * @param <E>
 */
public class LimitedQueue<E> extends LinkedList<E> { //LinkedList is the best implementation for this
    private int limit;

    /**
     *
     * @param limit the limit of the queue
     */
    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) { super.remove(); }
        return true;
    }
    @Override
    public void addLast(E o) {
        super.add(o);
        while (size() > limit) { super.remove(); }
    }
}
