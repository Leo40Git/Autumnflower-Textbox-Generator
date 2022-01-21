package adudecalledleo.aftbg.app.ui.worker;

import java.util.*;
import java.util.function.Consumer;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;

public final class FaceSearchWorker extends SwingWorker<Void, Face> {
    private final FaceCategory sourceCategory;
    private final String queryString;
    private final Consumer<List<Face>> resultHandler;

    public FaceSearchWorker(FaceCategory sourceCategory, String queryString, Consumer<List<Face>> resultHandler) {
        this.sourceCategory = sourceCategory;
        this.queryString = queryString;
        this.resultHandler = resultHandler;
    }

    private static final class LowerCaseCacheHelper {
        private final Map<String, String> cache;

        public LowerCaseCacheHelper(int capacity) {
            cache = new HashMap<>(capacity);
        }

        public String toLowerCase(String str) {
            return cache.computeIfAbsent(str, key -> key.toLowerCase(Locale.ROOT));
        }
    }

    @Override
    protected Void doInBackground() {
        // FIXME check if Thread.interrupted()
        var lcch = new LowerCaseCacheHelper(sourceCategory.getFaces().size());
        List<Face> toSort = new ArrayList<>();
        for (Face face : sourceCategory.getFaces().values()) {
            if (lcch.toLowerCase(face.getName()).contains(queryString)) {
                toSort.add(face);
            }
        }
        toSort.sort(Comparator.comparingInt(face -> lcch.toLowerCase(face.getName()).indexOf(queryString)));
        publish(toSort.toArray(Face[]::new));
        return null;
    }

    @Override
    protected void process(List<Face> chunks) {
        if (isCancelled()) {
            return;
        }
        resultHandler.accept(chunks);
    }
}
