package adudecalledleo.aftbg.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public final class PathUtils {
    private PathUtils() { }

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
        Files.delete(dir);
    }
}
