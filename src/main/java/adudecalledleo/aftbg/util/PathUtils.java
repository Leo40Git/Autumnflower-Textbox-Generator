package adudecalledleo.aftbg.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public final class PathUtils {
    private PathUtils() { }

    private static final Pattern SANITIZE_PATTERN = Pattern.compile("\\", Pattern.LITERAL);

    /**
     * Sanitizes a Windows path string, so it's valid across all platforms, by replacing {@code \} with {@code /}.
     *
     * @param path original path string
     * @return sanitized path string
     */
    public static String sanitize(String path) {
        return SANITIZE_PATTERN.matcher(path).replaceAll("/");
    }

    /**
     * Deletes a directory by recursively deleting its contents.
     *
     * @param dir the path to the directory to delete
     * @throws IOException if an I/O error occurs
     */
    public static void deleteDirectory(Path dir) throws IOException {
        if (Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
                for (Path entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        Files.deleteIfExists(dir);
    }

    /**
     * Tries to resolve a path string against another path.
     *
     * @param base the base path
     * @param other the path string to resolve against this path
     * @param description a description of the resulting path, for creating exceptions
     * @param exceptionConstructor a function that constructs an exception to throw if the path string is invalid
     * @param <E> the type of exception to throw
     * @return the resulting path
     * @throws E if the given path string cannot be converted to a Path.
     */
    public static <E extends Exception> Path tryResolve(Path base, String other, String description,
                                                        BiFunction<String, Throwable, E> exceptionConstructor) throws E {
        try {
            return base.resolve(other);
        } catch (InvalidPathException e) {
            throw exceptionConstructor.apply("Got invalid %s path: \"%s\"".formatted(description, other), e);
        }
    }

    /**
     * Tries to resolve a path string against another path.
     *
     * @param base the base path
     * @param other the path string to resolve against this path
     * @param description a description of the resulting path, for creating exceptions
     * @return the resulting path
     * @throws IllegalArgumentException if the given path string cannot be converted to a Path.
     */
    public static Path tryResolve(Path base, String other, String description) {
        return tryResolve(base, other, description, IllegalArgumentException::new);
    }

    /**
     * Tries to convert the given URI string into a {@link Path} object.
     *
     * @param rawUri the URI string to convert
     * @return the resulting {@code Path}.
     * @throws URISyntaxException      if the given string isn't a valid URI.
     * @throws InvalidPathURIException if the given string is a valid URI,
     *                                 but {@link Paths#get(URI)} failed to convert it into a path.
     * @see Paths#get(URI)
     */
    public static Path fromRawUri(String rawUri) throws URISyntaxException, InvalidPathURIException {
        URI uri = new URI(rawUri);
        try {
            return Paths.get(uri);
        } catch (Exception e) {
            throw new InvalidPathURIException("Failed to convert URI \"%s\" into Path".formatted(uri), e);
        }
    }

    public static class InvalidPathURIException extends Exception {
        private InvalidPathURIException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
