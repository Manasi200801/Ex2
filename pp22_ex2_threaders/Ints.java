import java.util.stream.IntStream;
import java.util.stream.Collectors;

public final class Ints {
    
    private Ints() {}

    public static int log2i(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    public static boolean isPowerOf2(int x) {
        return (x & (x - 1)) == 0;
    }

    public static String join(int[] values, String delimiter) {
        return IntStream.of(values)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(delimiter));
    }
}