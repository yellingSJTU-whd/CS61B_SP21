package deque;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

import static java.util.stream.IntStream.range;

public class LinkedListDeque<T> implements Deque<T> {
    private final Node<T> sentinel;

    private int size;

    public LinkedListDeque() {
        sentinel = new Node<>(null, null, null);
        size = 0;
        sentinel.pre = sentinel;
        sentinel.next = sentinel;
    }

    @Override
    public void addFirst(@NotNull T item) {
        Objects.requireNonNull(item);
        var second = sentinel.next;
        var first = new Node<>(item, sentinel, second);
        sentinel.next = first;
        second.pre = first;
        size++;
    }

    @Override
    public void addLast(@NotNull T item) {
        Objects.requireNonNull(item);
        var preLast = sentinel.pre;
        var last = new Node<>(item, preLast, sentinel);
        sentinel.pre = last;
        preLast.next = last;
        size++;
    }

    @Override
    public boolean isEmpty() {
        return sentinel.next == sentinel;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        var builder = new StringBuilder();
        for (T t : this) {
            builder.append(t).append(" ");
        }
        System.out.println(builder.toString().trim());
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node<T> toRemove = sentinel.next;
        Node<T> first = toRemove.next;
        toRemove.pre = null;
        toRemove.next = null;
        sentinel.next = first;
        first.pre = sentinel;
        size--;
        return toRemove.item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node<T> toRemove = sentinel.pre;
        Node<T> last = toRemove.pre;
        toRemove.pre = null;
        toRemove.next = null;
        sentinel.pre = last;
        last.next = sentinel;
        size--;
        return toRemove.item;
    }

    @Override
    public T get(int index) {
        return new DequeIterator().get(index);
    }

    public T getRecursive(int index) {
        if (index > size - 1) {
            return null;
        }
        return getRecursive(sentinel, index);
    }

    private T getRecursive(Node<T> node, int index) {
        if (index % size == 0) {
            return node.item;
        }
        if (index < size / 2) {
            return getRecursive(node.next, index - 1);
        }
        return getRecursive(node.pre, index + 1);
    }

    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    private static class Node<T> {
        T item;
        Node<T> pre;
        Node<T> next;

        Node(T item, Node<T> pre, Node<T> next) {
            this.item = item;
            this.pre = pre;
            this.next = next;
        }
    }

    private class DequeIterator implements Iterator<T> {
        Node<T> cursor;

        DequeIterator() {
            cursor = sentinel;
        }

        @Override
        public boolean hasNext() {
            return cursor.next != sentinel;
        }

        @Override
        public T next() {
            cursor = cursor.next;
            return cursor.item;
        }

        public T pre() {
            cursor = cursor.pre;
            return cursor.item;
        }

        private T get(int index) {
            if (index > size - 1) {
                return null;
            }
            if (index < size / 2) {
                range(0, index - 1).forEach(i -> cursor = cursor.next);
                return next();
            }
            range(0, size - index - 1).forEach(i -> cursor = cursor.pre);
            return pre();
        }
    }
}
