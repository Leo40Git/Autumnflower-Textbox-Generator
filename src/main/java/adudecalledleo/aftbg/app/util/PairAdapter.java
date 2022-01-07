package adudecalledleo.aftbg.app.util;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public final class PairAdapter<L, R> extends TypeAdapter<Pair<L, R>> {
    public static final TypeAdapterFactory FACTORY = new Factory();

    private static final class Factory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Type type = typeToken.getType();
            if (typeToken.getRawType() != Pair.class || !(type instanceof ParameterizedType pType)) {
                return null;
            }

            Type[] typeArgs = pType.getActualTypeArguments(); // { L, R }
            TypeAdapter<?> leftAdapter = gson.getAdapter(TypeToken.get(typeArgs[0]));
            TypeAdapter<?> rightAdapter = gson.getAdapter(TypeToken.get(typeArgs[1]));

            return (TypeAdapter<T>) new PairAdapter<>(leftAdapter, rightAdapter);
        }
    }

    private final TypeAdapter<L> leftAdapter;
    private final TypeAdapter<R> rightAdapter;

    public PairAdapter(TypeAdapter<L> leftAdapter, TypeAdapter<R> rightAdapter) {
        this.leftAdapter = leftAdapter;
        this.rightAdapter = rightAdapter;
    }

    @Override
    public Pair<L, R> read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        in.beginObject();
        boolean gotLeft = false, gotRight = false;
        L left = null;
        R right = null;

        while (in.hasNext()) {
            switch (in.nextName()) {
                case "left" -> {
                    left = leftAdapter.read(in);
                    gotLeft = true;
                }
                case "right" -> {
                    right = rightAdapter.read(in);
                    gotRight = true;
                }
            }
        }

        if (!gotLeft) {
            throw new IllegalStateException("Pair missing required value 'left'");
        }
        if (!gotRight) {
            throw new IllegalStateException("Pair missing required value 'right'");
        }

        in.endObject();
        return new Pair<>(left, right);
    }

    @Override
    public void write(JsonWriter out, Pair<L, R> value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        out.name("left");
        leftAdapter.write(out, value.left());
        out.name("right");
        rightAdapter.write(out, value.right());
        out.endObject();
    }
}
