package hashmap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.Objects;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    private static final int DEFAULT_INITIAL_SIZE = 16;

    private static final double DEFAULT_LOAD_FACTOR = .75;

    private final double loadFactor;

    private int size;

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!

    /**
     * Constructors
     */
    public MyHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        if (maxLoad < .0) {
            throw new IllegalArgumentException("invalid load factor: " + maxLoad);
        }
        buckets = createTable(nextPowerOfTwo(initialSize));
        loadFactor = maxLoad;
    }

    private static int nextPowerOfTwo(int num) {
        var initial = 1;
        while (initial < num) {
            initial <<= 1;
        }
        return initial;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    @SuppressWarnings("unchecked")
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    @Override
    public void clear() {
        int index = 0, n = buckets.length;
        while (++index < n) {
            var bucket = buckets[index];
            if (bucket == null) {
                continue;
            }
            bucket.clear();
            buckets[index] = null;
        }
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    @Override
    public V get(K key) {
        var node = getNode(key);
        return node == null ? null : node.value;
    }

    private Node getNode(K key) {
        Objects.requireNonNull(key);
        var hash = Objects.hashCode(key);
        var bucket = buckets[hash & (buckets.length - 1)];
        if (bucket == null) {
            return null;
        }
        for (var node : bucket) {
            if (Objects.equals(key, node.key)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key);
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            buckets[index] = createBucket();
        }
        var bucket = buckets[index];
        for (Node node : bucket) {
            if (Objects.equals(key, node.key)) {
                node.value = value;
                return;
            }
        }
        bucket.add(createNode(key, value));
        size++;

        if (size >= buckets.length * loadFactor) {
            resize(2 * buckets.length);
        }
    }

    private void resize(int newCap) {
        var newBuckets = createTable(newCap);

        for (var bucket : buckets) {
            if (bucket == null) {
                continue;
            }
            for (var node : bucket) {
                var index = Objects.hashCode(node.key) & (newCap - 1);
                if (newBuckets[index] == null) {
                    newBuckets[index] = createBucket();
                }
                var newBucket = newBuckets[index];
                newBucket.add(node);
            }
        }
        buckets = newBuckets;
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public V remove(K key) {
        Objects.requireNonNull(key, "calling remove() with a null key");
        int index = Objects.hashCode(key) & (buckets.length - 1);
        var bucket = buckets[index];
        if (bucket == null) {
            return null;
        }
        return bucket.stream()
                .filter(node -> Objects.equals(key, node.key))
                .findFirst().map(node -> {
                    bucket.remove(node);
                    size--;
                    return node.value;
                }).orElse(null);
    }

    @Override
    public V remove(K key, V value) {
        Objects.requireNonNull(key, "calling remove() with a null key");
        int index = Objects.hashCode(key) & (buckets.length - 1);
        var bucket = buckets[index];
        if (bucket == null) {
            return null;
        }
        return bucket.stream()
                .filter(node -> Objects.equals(key, node.key) && Objects.equals(value, node.value))
                .findFirst().map(node -> {
                    bucket.remove(node);
                    size--;
                    return node.value;
                }).orElse(null);
    }

    @Override
    public Iterator<K> iterator() {
        return Arrays.stream(buckets)
                .filter(Objects::nonNull)
                .map(Collection::stream)
                .reduce(Stream::concat)
                .map(nodeStream -> nodeStream.map(node -> node.key))
                .orElse(Stream.empty())
                .iterator();
    }

    private class KeySet extends AbstractSet<K> {
        @Override
        public final Iterator<K> iterator() {
            return MyHashMap.this.iterator();
        }

        @Override
        public int size() {
            return size;
        }
    }
}
