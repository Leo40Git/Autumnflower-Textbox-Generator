package adudecalledleo.aftbg.app.game;

import org.jetbrains.annotations.ApiStatus;

public abstract class DefinedObject {
    protected Definition source;

    public Definition getSource() {
        return source;
    }

    @ApiStatus.Internal
    public void setSource(Definition source) {
        this.source = source;
    }
}
