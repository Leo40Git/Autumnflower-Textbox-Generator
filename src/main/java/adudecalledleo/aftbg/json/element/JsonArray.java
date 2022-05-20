package adudecalledleo.aftbg.json.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

public final class JsonArray extends JsonElement implements Iterable<JsonElement> {
    private final List<JsonElement> values;

    JsonArray(List<JsonElement> values, boolean dummy) {
        this.values = values;
    }

    public JsonArray(List<JsonElement> values) {
        this.values = new ArrayList<>(values);
    }

    public JsonArray() {
        this.values = new ArrayList<>();
    }

    @Override
    public Type getType() {
        return Type.ARRAY;
    }

    public List<JsonElement> getValues() {
        return values;
    }

    @NotNull
    @Override
    public Iterator<JsonElement> iterator() {
        return values.iterator();
    }

    @Override
    public void forEach(Consumer<? super JsonElement> action) {
        values.forEach(action);
    }

    @Override
    public Spliterator<JsonElement> spliterator() {
        return values.spliterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonArray that = (JsonArray) o;

        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
