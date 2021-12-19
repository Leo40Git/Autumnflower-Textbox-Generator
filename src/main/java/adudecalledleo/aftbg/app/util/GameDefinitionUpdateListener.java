package adudecalledleo.aftbg.app.util;

import java.nio.file.Path;

import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.app.game.GameDefinition;

@FunctionalInterface
public interface GameDefinitionUpdateListener {
    void updateGameDefinition(Path basePath, GameDefinition gameDef, FacePool facePool);
}
