/**
 * Class that prints the Collatz sequence starting from a given number.
 *
 * @author YOUR NAME HERE
 */
public class Collatz {

    /**
     * Buggy implementation of nextNumber!
     */
    public static int nextNumber(int n) {
        if (isEven(n)) {
            return n / 2;
        } else {
            return 3 * n + 1;
        }
    }

    private static boolean isEven(int n) {
        return n % 2 == 0;
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.print(n + " ");
        while (n != 1) {
            n = nextNumber(n);
            System.out.print(n + " ");
        }
        System.out.println();
    }
}

