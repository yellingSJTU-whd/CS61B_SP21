package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.print("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        var counter = new AList<Integer>();
        int i = 1;
        while (i <= Math.pow(2, 7)) {
            counter.addLast(i * 1000);
            i *= 2;
        }
        var timer = new AList<Double>();
        var tester = new AList<Integer>();
        var count = 0;
        var index = 0;

        Stopwatch watch = new Stopwatch();
        while (index < counter.size()) {
            while (count <= counter.get(index)) {
                tester.addLast(0);
                count++;
            }
            timer.addLast(watch.elapsedTime());
            index++;
        }
        printTimingTable(counter, timer, counter);
    }
}
