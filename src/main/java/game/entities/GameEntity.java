package game.entities;

import game.logic.Collidable;
import game.logic.Renderable;
import game.logic.Updatable;
import javafx.geometry.Rectangle2D;

/**
 * Base type for all in-game entities that have position, size, and active state.
 */
public abstract class GameEntity implements Updatable, Renderable, Collidable {

    /** Current x-coordinate in world space. */
    protected double x;
    /** Current y-coordinate in world space. */
    protected double y;
    /** Entity width in world units. */
    protected final double width;
    /** Entity height in world units. */
    protected final double height;
    /**
     * Internal state field for active.
     */
    private boolean active = true;

    /**
     * Creates an entity with the given initial position and size.
     *
     * @param x initial x-coordinate
     * @param y initial y-coordinate
     * @param width entity width
     * @param height entity height
     */
    protected GameEntity(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the bounds.
     *
     * @return the bounds
     */
    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    /**
     * Checks whether active.
     *
     * @return true when the condition is met; otherwise false
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Executes deactivate.
     */
    public void deactivate() {
        active = false;
    }
}
