package deque;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

public class ArrayDeque<T> implements Deque<T> {

    private T[] items;
    private int head;
    private int tail;


    public ArrayDeque() {
        items = (T[]) new Object[8];
        head = 0;
        tail = 0;
    }

    @Override
    public void addFirst(@NotNull T item) {
        Objects.requireNonNull(item);
    }

    @Override
    public void addLast(@NotNull T item) {

    }

    /**
     * Circularly increase i by 1, mod modulus.
     */
    @Contract(pure = true)
    private static int inc(int i, int modulus) {
        if (++i >= modulus) i = 0;
        return i;
    }

    /**
     * Circularly decrease i by 1, mod modulus.
     */
    @Contract(pure = true)
    private static int dec(int i, int modulus) {
        if (--i < 0) i = modulus - 1;
        return i;
    }

    /**
     * Subtracts j from i, mod modulus.
     */
    @Contract(pure = true)
    private static int sub(int i, int j, int modulus) {
        if ((i -= j) < 0) i += modulus;
        return i;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void printDeque() {

    }

    @Override
    public T removeFirst() {
        return null;
    }

    @Override
    public T removeLast() {
        return null;
    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }
}
