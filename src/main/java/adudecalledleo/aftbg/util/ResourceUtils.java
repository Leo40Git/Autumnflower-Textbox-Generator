package adudecalledleo.aftbg.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class ResourceUtils {
    private ResourceUtils() { }

    public static InputStream getResourceAsStream(Class<?> baseClass, String path) throws IOException {
        var in = baseClass.getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException(path);
        }
        return in;
    }
}
