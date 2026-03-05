package game.logic;

import javafx.geometry.Rectangle2D;

/**
 * Defines an object that can provide a collision boundary.
 */
public interface Collidable {
    /**
     * Returns the current collision bounds in world coordinates.
     *
     * @return axis-aligned collision rectangle
     */
    Rectangle2D getBounds();
}
