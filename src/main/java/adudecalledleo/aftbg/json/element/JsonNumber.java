package adudecalledleo.aftbg.json.element;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class JsonNumber extends JsonElement {
    public static JsonNumber valueOf(Number value) {
        Objects.requireNonNull(value, "value");
        return Cache.getOrCreate(value);
    }

    private final Number value;

    public JsonNumber(Number value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.NUMBER;
    }

    public Number getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonNumber that = (JsonNumber) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    private static final class Cache {
        private static final Map<Number, JsonNumber> CACHE;

        static {
            CACHE = new HashMap<>();
            for (int i = -128; i <= 127; i++) {
                add(i);
            }
            add(BigInteger.ZERO);
            add(BigInteger.ONE);
            add(BigInteger.TWO);
            add(BigInteger.TEN);
            add(BigDecimal.ZERO);
            add(BigDecimal.ONE);
            add(BigDecimal.TEN);
        }

        private static void add(Number value) {
            CACHE.put(value, new JsonNumber(value));
        }

        public static JsonNumber getOrCreate(Number value) {
            var n = CACHE.get(value);
            if (n == null) {
                n = new JsonNumber(value);
            }
            return n;
        }
    }
}
