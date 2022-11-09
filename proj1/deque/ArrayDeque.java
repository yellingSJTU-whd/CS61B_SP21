package deque;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ArrayDeque<T> implements Deque<T> {

    /**
     * The underlying array in which the items of the deque are stored.
     * Invariability:
     * 1. All array cells not holding deque items are null.
     */
    private T[] items;

    /**
     * The index of the item at the head of this deque.
     * Invariability:
     * 1. 0 <= head <= items.length.
     * 2. head == tail means this deque is empty.
     */
    private int head;

    /**
     * The index at which the next item would be added to the tail of this deque.
     * Invariability:
     * 1. tail stands right before head circularly means this deque is full.
     */
    private int tail;
    private static final int INITIAL_SIZE = 8;
    private static final double USAGE_FACTOR_THRESHOLD = 0.25;

    @SuppressWarnings("unchecked")
    public ArrayDeque() {
        items = (T[]) new Object[INITIAL_SIZE];
    }

    @Override
    public void addFirst(T item) {
        Objects.requireNonNull(item);

        head = dec(head, items.length);
        items[head] = item;

        growIfDequeFull();
    }

    @Override
    public void addLast(T item) {
        Objects.requireNonNull(item);

        items[tail] = item;
        tail = inc(tail, items.length);

        growIfDequeFull();
    }

    /**
     * Resize the underlying array to the specified capacity, also reset head to 0.
     */
    @SuppressWarnings("unchecked")
    private void resize(int capacity) {
        T[] resized = (T[]) new Object[capacity];
        if (tail < head) {
            System.arraycopy(items, head, resized, 0, items.length - head);
            System.arraycopy(items, 0, resized, items.length - head, tail);
        } else {
            System.arraycopy(items, head, resized, 0, tail - head);
        }
        head = 0;
        tail = size();
        items = resized;
    }

    /**
     * Circularly increase i by 1, mod modulus.
     */
    private static int inc(int i, int modulus) {
        if (++i >= modulus) {
            i = 0;
        }
        return i;
    }

    /**
     * Circularly decrease i by 1, mod modulus.
     */
    private static int dec(int i, int modulus) {
        if (--i < 0) {
            i = modulus - 1;
        }
        return i;
    }

    /**
     * Subtracts j from i, mod modulus.
     * The return value is equals to circular distance between i and j.
     */
    private static int sub(int i, int j, int modulus) {
        i -= j;
        if (i < 0) {
            i += modulus;
        }
        return i;
    }

    @Override
    public int size() {
        return sub(tail, head, items.length);
    }

    @Override
    public boolean isEmpty() {
        return head == tail;
    }

    @Override
    public void printDeque() {
        var builder = new StringBuilder();
        for (T item : this) {
            builder.append(item).append(" ");
        }
        System.out.println(builder.toString().trim());
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        shrinkIfNeed();

        T toRemove = items[head];
        items[head] = null;
        head = inc(head, items.length);
        return toRemove;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        shrinkIfNeed();

        tail = dec(tail, items.length);
        T toRemove = items[tail];
        items[tail] = null;
        return toRemove;
    }

    /**
     * Double the capacity of this deque, if it is full.
     */
    private void growIfDequeFull() {
        if (head == inc(tail, items.length)) {
            resize(items.length * 2);
        }
    }

    /**
     * Halves the capacity of this deque, if usage factor went below threshold after removal
     * AND size is greater than initial.
     */
    private void shrinkIfNeed() {
        var length = items.length;
        if ((size() - 1) / (double) length < USAGE_FACTOR_THRESHOLD && length > INITIAL_SIZE) {
            resize(items.length / 2);
        }
    }

    @Override
    public T get(int index) {
        if (index > size() - 1) {
            return null;
        }
        return items[head + index];
    }

    @Override
    public Iterator<T> iterator() {
        if (head > tail) {
            return Stream.concat(Arrays.stream(items, head, items.length),
                    Arrays.stream(items, 0, tail)).iterator();
        }
        return Arrays.stream(items, head, tail).iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deque)) {
            return false;
        }
        var that = (Deque<T>) o;
        if (size() != that.size()) {
            return false;
        }
        return IntStream.range(0, size()).noneMatch(i -> get(i) != that.get(i));
    }
}
