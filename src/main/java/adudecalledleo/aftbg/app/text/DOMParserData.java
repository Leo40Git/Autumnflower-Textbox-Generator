package adudecalledleo.aftbg.app.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public final class DOMParserData {
    public record Key<T>(Class<? extends T> type, String name) { }

    public static <T> Key<T> key(Class<? extends T> type, String name) {
        return new Key<>(type, name);
    }

    public record Entry<T>(Key<T> key, T value) { }

    private final Map<Key<?>, Object> values;

    public DOMParserData() {
        this.values = new HashMap<>();
    }

    public int size() {
        return values.size();
    }

    public <T> Optional<T> get(Key<T> key) {
        Object rawValue = values.get(key);
        if (!key.type().isInstance(rawValue)) {
            return Optional.empty();
        }
        return Optional.of(key.type().cast(rawValue));
    }

    public <T> DOMParserData set(Key<T> key, T value) {
        values.put(key, value);
        return this;
    }

    public DOMParserData remove(Key<?> key) {
        values.remove(key);
        return this;
    }

    public void clear() {
        values.clear();
    }

    public boolean containsKey(Key<?> key) {
        return values.containsKey(key);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    @NotNull
    public Iterator<Entry<?>> iterator() {
        return new EntryIterator(values.entrySet().iterator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DOMParserData that = (DOMParserData) o;

        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    protected static class EntryIterator implements Iterator<Entry<?>> {
        protected final Iterator<Map.Entry<Key<?>, Object>> wrapped;

        public EntryIterator(Iterator<Map.Entry<Key<?>, Object>> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean hasNext() {
            return wrapped.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Entry<?> next() {
            var entry = wrapped.next();
            return new Entry<>((Key<Object>) entry.getKey(), entry.getValue());
        }
    }
}
