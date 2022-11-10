package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.*;


/**
 * Performs some basic linked list tests.
 */
public class LinkedListDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {
        LinkedListDeque<String> lld1 = new LinkedListDeque<>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {
        LinkedListDeque<String> lld1 = new LinkedListDeque<>();
        LinkedListDeque<Double> lld2 = new LinkedListDeque<>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();

        assertEquals("string", s);
        assertEquals(3.14159, d, 10e-7);
        assertTrue(b);
    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertNull("Should return null when removeFirst is called on an empty Deque,", lld1.removeFirst());
        assertNull("Should return null when removeLast is called on an empty Deque,", lld1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    public void randomized_test() {
        var deque = new LinkedListDeque<Integer>();
        int count = 5000;
        AtomicInteger firstItem = new AtomicInteger(-1);
        AtomicInteger lastItem = new AtomicInteger(-1);
        AtomicInteger size = new AtomicInteger();
        IntStream.range(0, count).forEachOrdered(i -> {
            int random = StdRandom.uniform(0, 5);
            switch (random) {
                case 0 -> {
                    deque.addFirst(i);
                    size.getAndIncrement();
                    firstItem.set(i);
                    System.out.printf("add %s at front%n", i);
                    deque.printDeque();
                }
                case 1 -> {
                    deque.addLast(i);
                    size.getAndIncrement();
                    lastItem.set(i);
                    System.out.printf("add %s at rear%n", i);
                    deque.printDeque();
                }
                case 2 -> {
                    System.out.printf("checking isEmpty()%n");
                    deque.printDeque();
                    assertEquals(size.get() == 0, deque.isEmpty());
                }
                case 3 -> {
                    System.out.printf("checking size()%n");
                    deque.printDeque();
                    assertEquals(size.get(), deque.size());
                }
                case 4 -> {
                    System.out.printf("checking get: expected size = %s, real size = %s%n", size.get(), deque.size());
                    deque.printDeque();
                    if (firstItem.get() != -1) {
                        assertEquals(firstItem.get(), deque.get(0).intValue());
                    }
                    if (lastItem.get() != -1) {
                        assertEquals(lastItem.get(), deque.get(size.get() - 1).intValue());
                    }
                }
            }
        });
    }

    @Test
    public void test_get() {
        int count = 100;
        var deque = new LinkedListDeque<Integer>();
        IntStream.range(0, count).forEachOrdered(deque::addLast);
        IntStream.range(0, count).forEachOrdered(i -> {
            assertEquals(i, deque.getRecursive(i).intValue());
        });
//        assertEquals(1,deque.get(1).intValue());
    }
}
