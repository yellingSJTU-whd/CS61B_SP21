package deque;

public interface Deque<T> {
    /**
     * AInsert the specified item at the front of this deque.
     */
    void addFirst(T item);

    void addLast( T item);

    default boolean isEmpty() {
        return size() == 0;
    }

    int size();

    void printDeque();

    T removeFirst();

    T removeLast();

    T get(int index);

    boolean equals(Object o);
}
