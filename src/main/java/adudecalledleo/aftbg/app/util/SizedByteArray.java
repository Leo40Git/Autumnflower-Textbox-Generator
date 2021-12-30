package adudecalledleo.aftbg.app.util;

/**
 * Represents a {@code byte} array with a specific size, that is possibly different to the array's actual length.
 */
public record SizedByteArray(int size, byte[] bytes) { }
