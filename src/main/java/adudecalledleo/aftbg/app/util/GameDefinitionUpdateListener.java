package adudecalledleo.aftbg.app.util;

import adudecalledleo.aftbg.app.game.GameDefinition;

@FunctionalInterface
public interface GameDefinitionUpdateListener {
    void updateGameDefinition(GameDefinition gameDef);
}
