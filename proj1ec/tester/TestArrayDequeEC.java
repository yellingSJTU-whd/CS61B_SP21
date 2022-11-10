package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

import java.util.stream.IntStream;

public class TestArrayDequeEC {

    @Test
    public void test() {
        int count = 10000;
        var student = new StudentArrayDeque<Integer>();
        var reference = new ArrayDequeSolution<Integer>();
        var builder = new StringBuilder(String.format("%n"));
        IntStream.range(0, count).forEachOrdered(i -> {
            switch (StdRandom.uniform(0, 4)) {
                case 0 -> {
                    builder.append(String.format("addFirst(%s)%n", i));
                    student.addFirst(i);
                    reference.addFirst(i);
                }
                case 1 -> {
                    builder.append(String.format("addLast(%s)%n", i));
                    student.addLast(i);
                    reference.addLast(i);
                }
                case 2 -> {
                    builder.append(String.format("removeFirst()%n"));
                    if (!reference.isEmpty()) {
                        assertEquals(builder.toString(),
                                reference.removeFirst().intValue(), student.removeFirst().intValue());
                    }
                }
                case 3 -> {
                    builder.append(String.format("removeLast()%n"));
                    if (!reference.isEmpty()) {
                        assertEquals(builder.toString(),
                                reference.removeLast().intValue(), student.removeLast().intValue());
                    }
                }
                default -> {

                }
            }
        });
    }
}
