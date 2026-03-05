package game.logic;

import javafx.scene.canvas.GraphicsContext;

/**
 * Defines an object that can draw itself on a JavaFX canvas.
 */
public interface Renderable {
    /**
     * Renders this object using the provided graphics context.
     *
     * @param gc graphics context used for drawing
     */
    void render(GraphicsContext gc);
}
