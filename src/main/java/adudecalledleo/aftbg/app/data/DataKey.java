package adudecalledleo.aftbg.app.data;

public record DataKey<T>(Class<? extends T> type, String name) { }
