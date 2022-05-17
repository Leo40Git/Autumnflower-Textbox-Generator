package adudecalledleo.aftbg.app.ui.worker;

import java.util.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;

public class FaceSearchWorker extends SwingWorker<Void, Face> {
    private final FaceCategory sourceCategory;
    private final String queryString;

    public FaceSearchWorker(FaceCategory sourceCategory, String queryString) {
        this.sourceCategory = sourceCategory;
        this.queryString = queryString;
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
        var lcch = new LowerCaseCacheHelper(sourceCategory.getFaces().size());
        List<Face> toSort = new ArrayList<>();
        for (Face face : sourceCategory.getFaces().values()) {
            if (Thread.interrupted()) {
                return null;
            }

            if (lcch.toLowerCase(face.getName()).contains(queryString)) {
                toSort.add(face);
            }
        }

        if (Thread.interrupted()) {
            return null;
        }

        toSort.sort(Comparator.comparingInt(face -> lcch.toLowerCase(face.getName()).indexOf(queryString)));
        publish(toSort.toArray(Face[]::new));
        return null;
    }
}
