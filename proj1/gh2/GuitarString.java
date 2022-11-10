package gh2;

import deque.ArrayDeque;
import deque.Deque;
import edu.princeton.cs.algs4.StdRandom;

import java.util.stream.IntStream;

//Note: This file will not compile until you complete the Deque implementations
public class GuitarString {
    /**
     * Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday.
     */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */
    private final Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {
        buffer = new ArrayDeque<>();
        int size = (int) Math.round(SR / frequency);
        IntStream.range(0, size).forEachOrdered(i -> {
            buffer.addLast(0.0);
        });
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        IntStream.range(0, buffer.size()).forEachOrdered(i -> {
            buffer.removeFirst();
            buffer.addLast(Math.random() - 0.5);
        });
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        double front = buffer.removeFirst();
        buffer.addLast(DECAY * 0.5 * (sample() + front));
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        return buffer.get(0);
    }
}
