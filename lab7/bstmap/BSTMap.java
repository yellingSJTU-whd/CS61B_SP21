package bstmap;

import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private Node root;

    @Override
    public void clear() {
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        return keySet().contains(key);
    }

    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "calling get() with a null key");
        if (root == null) {
            return null;
        }
        return get(root, key);
    }

    private V get(Node node, K key) {
        if (node == null) {
            return null;
        }
        var ref = key.compareTo(node.key);
        if (ref < 0) {
            return get(node.left, key);
        }
        if (ref > 0) {
            return get(node.right, key);
        }
        return node.value;
    }

    @Override
    public int size() {
        return size(root);
    }

    private int size(Node node) {
        if (node == null) {
            return 0;
        }
        return node.size;
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "calling put() with a null key");
        if (value == null) {
            remove(key);
        }
        root = put(root, key, value);
    }

    /**
     * Insert a key-value pair into this BST map, starting at the specified node.
     * Return the BST with the key-value pair added, rooted at the specified node.
     */
    private Node put(Node node, K key, V value) {
        if (node == null) {
            return new Node(key, value, 1);
        }
        var ref = key.compareTo(node.key);
        if (ref < 0) {
            node.left = put(node.left, key, value);
        }
        if (ref > 0) {
            node.right = put(node.right, key, value);
        }
        node.size = size(node.left) + size(node.right) + 1;
        return node;
    }

    @Override
    public Set<K> keySet() {
        var keySet = new HashSet<K>();
        helper(root, keySet);
        return keySet;
    }

    /**
     * Helper method to build keySet recursively.
     */
    private void helper(Node node, Set<K> keySet) {
        if (node == null) {
            return;
        }
        keySet.add(node.key);
        helper(node.left, keySet);
        helper(node.right, keySet);
    }

    @Override
    public V remove(K key) {
        Objects.requireNonNull(key, "calling remove() with a null key");
        V value = get(key);
        if (value != null) {
            root = remove(key, root);
        }
        return value;
    }

    /**
     * Remove the key if presented as well as associating value from a subtree,
     * which rooted by the specified node. Return new root of the subtree.
     */
    private Node remove(K key, Node node) {
        if (node == null) {
            return null;
        }

        int ref = key.compareTo(node.key);
        if (ref < 0) {
            node.left = remove(key, node.left);
        } else if (ref > 0) {
            node.right = remove(key, node.right);
        } else {
            if (node.left != null && node.right != null) {
                Node next = min(node.right);
                node.key = next.key;
                node.value = next.value;
                node.right = remove(node.key, node.right);
            } else {
                return node.right == null ? node.left : node.right;
            }
        }

        node.size = size(node.left) + size(node.right) + 1;
        return node;
    }

    /**
     * Return the minimal node from a subtree, which rooted by the specified node.
     */
    private Node min(Node node) {
        Objects.requireNonNull(node);
        if (node.left == null) {
            return node;
        }
        return min(node.left);
    }

    @Override
    public V remove(K key, V value) {
        Objects.requireNonNull(key, "calling remove with a null key");
        V val = get(key);
        if (val.equals(value)) {
            root = remove(key, root);
            return val;
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    public void printInOrder() {
        var heap = new PriorityQueue<Node>(Comparator.comparing(node -> node.key));
        stuff(heap, root);

        var builder = new StringBuilder();
        while (!heap.isEmpty()) {
            Node node = heap.poll();
            builder.append(String.format("key = %s, value = %s%n", node.key, node.value));
        }
        System.out.println(builder.toString());
    }

    /**
     * Add all nodes in a subtree, rooted by the specified node, into the heap.
     */
    private void stuff(PriorityQueue<Node> heap, Node node) {
        if (node == null) {
            return;
        }
        heap.add(node);
        stuff(heap, node.left);
        stuff(heap, node.right);
    }

    private class Node {
        private K key;
        private V value;
        private Node left;
        private Node right;
        private int size;

        private Node(K k, V v, int size) {
            key = k;
            value = v;
            this.size = size;
        }
    }
}
