package deque;

import java.util.Iterator;

public interface Deque<T> extends Iterable<T> {
    /**
     * AInsert the specified item at the front of this deque.
     *
     * @param item - the item to add
     */
    public void addFirst(T item);

    public void addLast( T item);

    default boolean isEmpty() {
        return size() == 0;
    }

    public int size();

    public void printDeque();

    public T removeFirst();

    public T removeLast();

    public T get(int index);

    @Override
    public Iterator<T> iterator();

    public boolean equals(Object o);
}
