package deque;

import org.junit.Test;

import java.util.stream.IntStream;

import static java.lang.Math.floorMod;
import static org.junit.Assert.assertEquals;

public class ArrayDequeTest {

    @Test
    public void test_size() {
        int size = 100;
        var deque = new ArrayDeque<Integer>();
        IntStream.range(0, size).forEachOrdered(i -> {
            assertEquals(i, deque.size());
            deque.addLast(i);
        });
        assertEquals(size, deque.size());

        IntStream.range(0, size).forEachOrdered(i -> {
            assertEquals(size - i, deque.size());
            var item = deque.removeFirst();
            assertEquals(i, item.intValue());
        });
    }
}
