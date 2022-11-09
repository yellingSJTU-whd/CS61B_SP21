package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        comparator = c;
    }

    public T max() {
        return max(comparator);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T max = null;
        for (T item : this) {
            if (max == null) {
                max = item;
            } else {
                max = c.compare(item, max) < 0 ? max : item;
            }
        }
        return max;
    }

}
