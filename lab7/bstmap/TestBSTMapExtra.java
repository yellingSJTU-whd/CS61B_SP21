package bstmap;

import java.util.Set;
import java.util.HashSet;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests of optional parts of lab 7.
 */
public class TestBSTMapExtra {

    /*
     * Sanity test for keySet, only here because it's optional
     */
    @Test
    public void sanityKeySetTest() {
        BSTMap<String, Integer> b = new BSTMap<>();
        HashSet<String> values = new HashSet<>();
        for (int i = 0; i < 455; i++) {
            b.put("hi" + i, 1);
            values.add("hi" + i);
        }
        assertEquals(455, b.size()); //keys are there
        Set<String> keySet = b.keySet();
        assertTrue(values.containsAll(keySet));
        assertTrue(keySet.containsAll(values));
    }

    /* Remove Test
     *
     * Note for testRemoveRoot:
     *
     * Just checking that c is gone (perhaps incorrectly)
     * assumes that remove is BST-structure preserving.
     *
     * More exhaustive tests could be done to verify
     * implementation of remove, but that would require doing
     * things like checking for inorder vs. preorder swaps,
     * and is unnecessary in this simple BST implementation.
     */
    @Test
    public void testRemoveRoot() {
        BSTMap<String, String> q = new BSTMap<>();
        q.put("c", "a");
        q.put("b", "a");
        q.put("a", "a");
        q.put("d", "a");
        q.put("e", "a"); // a b c d e
        q.printInOrder();
        assertNotNull(q.remove("c"));
        q.printInOrder();
        assertFalse(q.containsKey("c"));
        assertTrue(q.containsKey("a"));
        assertTrue(q.containsKey("b"));
        assertTrue(q.containsKey("d"));
        assertTrue(q.containsKey("e"));
    }

    /* Remove Test 2
     * test the 3 different cases of remove
     */
    @Test
    public void testRemoveThreeCases() {
        BSTMap<String, String> q = new BSTMap<>();
        q.put("c", "a");
        q.put("b", "a");
        q.put("a", "a");
        q.put("d", "a");
        q.put("e", "a");                         // a b c d e
        assertNotNull(q.remove("e"));      // a b c d
        assertTrue(q.containsKey("a"));
        assertTrue(q.containsKey("b"));
        assertTrue(q.containsKey("c"));
        assertTrue(q.containsKey("d"));
        assertNotNull(q.remove("c"));      // a b d
        assertTrue(q.containsKey("a"));
        assertTrue(q.containsKey("b"));
        assertTrue(q.containsKey("d"));
        q.put("f", "a");                         // a b d f
        assertNotNull(q.remove("d"));      // a b f
        assertTrue(q.containsKey("a"));
        assertTrue(q.containsKey("b"));
        assertTrue(q.containsKey("f"));
    }

    /* Remove Test 3
     *  Checks that remove works correctly on root nodes
     *  when the node has only 1 or 0 children on either side. */
    @Test
    public void testRemoveRootEdge() {
        var rightChild = new BSTMap<Character, Integer>();
        rightChild.put('A', 1);
        rightChild.put('B', 2);
        Integer result = rightChild.remove('A');
        assertEquals(String.format("result = %s%n", result), 1, result.intValue());
        for (int i = 0; i < 10; i++) {
            rightChild.put((char) ('C' + i), 3 + i);
        }
        rightChild.put('A', 100);
        assertEquals(4, rightChild.remove('D').intValue());
        assertEquals(7, rightChild.remove('G').intValue());
        assertEquals(100, rightChild.remove('A').intValue());
        assertEquals(9, rightChild.size());

        var leftChild = new BSTMap<Character, Integer>();
        leftChild.put('B', 1);
        leftChild.put('A', 2);
        assertEquals(1, leftChild.remove('B').intValue());
        assertEquals(1, leftChild.size());
        assertNull(leftChild.get('B'));

        var noChild = new BSTMap<Character, Integer>();
        noChild.put('Z', 15);
        assertEquals(15, (int) ((Integer) noChild.remove('Z')));
        assertEquals(0, noChild.size());
        assertNull(noChild.get('Z'));
    }

}
