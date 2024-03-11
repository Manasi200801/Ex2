import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.function.IntBinaryOperator;
public class PrefixSum {
    // Original array
    private final int[] A;
    private final int n;
    private final IntBinaryOperator op;
    private final int p, k;
    // Prefix sum
    public volatile int[] S;
    public PrefixSum(int[] values, IntBinaryOperator operator) {
        n = values.length;
        if (!Ints.isPowerOf2(n)) {
            throw new IllegalArgumentException("Length is not a power of 2");
        }
        A = Arrays.copyOf(values, n);
        op = operator;
        k = Ints.log2i(n);
        p = n / k;
    }
    // Computes the prefix sum on `A[start..end]`
    private void computeSequential(int[] A, int start, int end) {
        for (int i = start + 1; i < end; i++) {
            A[i] = op.applyAsInt(A[i], A[i - 1]);
        }
    }
    // Applies the value `a` on `A[start..end]`
    private void applySequential(int[] A, int a, int start, int end) {
        for (int i = start; i < end; i++) {
            A[i] = op.applyAsInt(A[i], a);
        }
    }
    public void computeSequential() {
        S = Arrays.copyOf(A, n);
        computeSequential(S, 0, n);
    }
    public void computeParallel() {
        S = Arrays.copyOf(A, n);
        var executor = Executors.newFixedThreadPool(p);

        try {
            stage1(executor);
            int[] T = stage2();
            stage3(T, executor);
        } catch (InterruptedException exception) {
            // Ignore exception
        }
        executor.shutdownNow();
    }
    // Stage 1: Compute local prefix sum of sub-array per processor
    private void stage1(ExecutorService executor) throws InterruptedException {
        var latch = new CountDownLatch(p);
        for (int i = 0; i < p; i++) {
            int start = i * k, end = start + k;
            executor.submit(() -> {
                computeSequential(S, start, end);
                latch.countDown();
            });
        }
        latch.await();
    }
    // Stage 2: Compute prefix sum of rightmost values of each sub-array
    private int[] stage2() {
        int[] T = new int[p];
        for (int i = 0; i < p; i++) {
            T[i] = S[(k - 1) + i * k];
        }
        computeSequential(T, 0, T.length);
        return T;
    }
    // Stage 3: "Add" values of previous prefix sum to each sub-array, skipping the 1st
    private void stage3(int[] T, ExecutorService executor) throws InterruptedException {
        var latch = new CountDownLatch(p - 1);
        for (int i = 1; i < p; i++) {
            int j = i;
            int start = i * k, end = start + k;
            executor.submit(() -> {
                applySequential(S, T[j - 1], start, end);
                latch.countDown();
            });
        }
        latch.await();
    }
    public void print() {
        System.out.println(Ints.join(S, ", "));
    }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Input .csv file not provided");
            return;
        }
        int[] values;
        try {
            values = CsvReader.readInts(args[0]);
        } catch (Exception exception) {
            System.err.println("Input .csv file does not exist");
            return;
        }
        // Exercise 2.1
        var prefixSum = new PrefixSum(values, (x, y) -> x + y);
        prefixSum.computeParallel();
        prefixSum.print();

        // Exercise 2.2
        var compaction = new Compaction(values);
        compaction.computeParallel();
        compaction.print();
    }
}