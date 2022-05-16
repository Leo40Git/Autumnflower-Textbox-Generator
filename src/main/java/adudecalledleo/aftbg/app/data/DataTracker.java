package adudecalledleo.aftbg.app.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class DataTracker {
    public record Entry<T>(DataKey<T> key, T value) { }

    private final Map<DataKey<?>, Object> values;

    public DataTracker() {
        this.values = new HashMap<>();
    }

    public int size() {
        return values.size();
    }

    public <T> Optional<T> get(DataKey<T> key) {
        Object rawValue = values.get(key);
        if (!key.type().isInstance(rawValue)) {
            return Optional.empty();
        }
        return Optional.of(key.type().cast(rawValue));
    }

    public <T> DataTracker set(DataKey<T> key, T value) {
        values.put(key, value);
        return this;
    }

    public DataTracker remove(DataKey<?> key) {
        values.remove(key);
        return this;
    }

    public void clear() {
        values.clear();
    }

    public boolean containsKey(DataKey<?> key) {
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
        if (o instanceof DataTracker tracker) {
            if (size() != tracker.size()) {
                return false;
            }
            var it1 = iterator();
            var it2 = tracker.iterator();
            while (it1.hasNext() && it2.hasNext()) {
                if (!it1.next().equals(it2.next())) {
                    return false;
                }
            }
            return !it1.hasNext() && !it2.hasNext();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    protected static class EntryIterator implements Iterator<Entry<?>> {
        protected final Iterator<Map.Entry<DataKey<?>, Object>> wrapped;

        public EntryIterator(Iterator<Map.Entry<DataKey<?>, Object>> wrapped) {
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
            return new Entry<>((DataKey<Object>) entry.getKey(), entry.getValue());
        }
    }
}
