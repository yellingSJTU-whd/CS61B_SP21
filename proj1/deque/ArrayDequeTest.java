package deque;

import org.jetbrains.annotations.Contract;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;

import static java.lang.Math.floorMod;
import static org.junit.Assert.*;

public class ArrayDequeTest {

    @Test
    public void test_resize() {
        int size = 100;
        int i = 5;
        int j =10;
        System.out.printf("sub returns %s, floorMod returns %s%n", sub(i,j,size), floorMod(5,10));
    }

    @Contract(pure = true)
    private static int sub(int i, int j, int modulus) {
        i -= j;
        if (i < 0) {
            i += modulus;
        }
        return i;
    }
}
