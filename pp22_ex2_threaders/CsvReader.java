import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CsvReader {

    private static final Pattern SEPARATOR = Pattern.compile(",\s*");

    private CsvReader() {}

    private static <T> T tryParse(String value, Function<String, T> parser) {
        try {
            return parser.apply(value);
        } catch (Exception exception) {
            return null;
        }
    }

    private static <T> Stream<T> splitAndParse(
            Path path, Function<String, T> parser) throws IOException {
        return Files.lines(path)
                .flatMap(line -> SEPARATOR.splitAsStream(line))
                .mapMulti((rawValue, mapper) -> {
                    T value = tryParse(rawValue, parser);
                    if (value != null) {
                        mapper.accept(value);
                    }
                });
    }

    public static int[] readInts(String path) throws IOException {
        return splitAndParse(Paths.get(path), Double::parseDouble)
               .mapToInt(Double::intValue)
               .toArray();
    }
}