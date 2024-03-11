import java.util.Arrays;

public class Compaction {

    // Original array
    private final int[] A;
    private final int n;

    private final int p, k;

    // Compact array and coordinates
    public volatile int[] V;
    public volatile int[] C;

    public Compaction(int[] values) {
        n = values.length;
        if (!Ints.isPowerOf2(n)) {
            throw new IllegalArgumentException("Length is not a power of 2");
        }

        A = Arrays.copyOf(values, n);

        k = Ints.log2i(n);
        p = n / k;
    }

    public void computeSequential() {
        V = new int[n];
        C = new int[n];

        int j = 0;
        for (int i = 0; i < n; i++) {
            if (A[i] != 0) {
                V[j] = A[i];
                C[j] = i;
                j++;
            }
        }

        V = Arrays.copyOfRange(V, 0, j);
        C = Arrays.copyOfRange(C, 0, j);
    }

    public void computeParallel() {
        V = new int[n];
        C = new int[n];

        int[] T = stage1();
        int[] S = stage2(T);
        int limit = stage3(S);

        V = Arrays.copyOfRange(V, 0, limit);
        C = Arrays.copyOfRange(C, 0, limit);
    }

    // Stage 1: Create temporary array where `T[i] = 1 <=> A[i] != 0`
    private int[] stage1() {
        int[] T = new int[n];
        for (int i = 0; i < n; i++) {
            T[i] = A[i] != 0 ? 1 : 0;
        }
        return T;
    }

    // Stage 2: Compute parallel prefix sum on said array
    private int[] stage2(int[] T) {        
        var prefixSum = new PrefixSum(T, (a, b) -> a + b);
        prefixSum.computeSequential();

        return prefixSum.S;
    }

    // Stage 3: Filter non-zero values and derive coordinates
    private int stage3(int[] S) {
        int j = 0;
        for (int i = 0; i < n; i++) {
            if (S[i] == j + 1) {
                V[j] = A[i];
                C[j] = i;
                j++;
            }
        }

        return j;
    }

    public void print() {
        System.out.println(Ints.join(V, ", "));
        System.out.println(Ints.join(C, ", "));
    }
}