package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        var aListNoResizing = new AListNoResizing<Integer>();
        var buggyAList = new AListNoResizing<Integer>();
        aListNoResizing.addLast(4);
        aListNoResizing.addLast(5);
        aListNoResizing.addLast(6);
        buggyAList.addLast(4);
        buggyAList.addLast(5);
        buggyAList.addLast(6);

        assertEquals(aListNoResizing.removeLast(), buggyAList.removeLast());
        assertEquals(aListNoResizing.removeLast(), buggyAList.removeLast());
        assertEquals(aListNoResizing.removeLast(), buggyAList.removeLast());
    }

    @Test
    public void randomizedTest() {
        var l = new AListNoResizing<Integer>();
        var b = new BuggyAList<Integer>();

        int N = 50000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            switch (operationNumber) {
                case 0:
                    // addLast
                    int randVal = StdRandom.uniform(0, 100);
                    l.addLast(randVal);
                    b.addLast(randVal);
                    break;
                case 1:
                    // size
                    assertEquals(l.size(), b.size());
                    break;
                case 2:
                    // getLast
                    if (l.size() > 0) {
                        assertEquals(l.getLast(), b.getLast());
                    }
                    break;
                case 3:
                    // removeLast
                    if (l.size() > 0) {
                        assertEquals(l.removeLast(), b.removeLast());
                    }
                    break;
            }
        }
    }
}
