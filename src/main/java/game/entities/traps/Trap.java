package game.entities.traps;

import game.entities.GameEntity;

/**
 * Base type for map traps that can be triggered and apply explosion force.
 */
public abstract class Trap extends GameEntity {

    /**
     * Creates a trap with explicit bounds.
     *
     * @param x trap x-coordinate
     * @param y trap y-coordinate
     * @param width trap width
     * @param height trap height
     */
    protected Trap(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    /**
     * Updates this object state for the current frame.
     *
     * @param deltaSeconds parameter value
     */
    @Override
    public void update(double deltaSeconds) {

    }


    /**
     * Returns the explosion force multiplier.
     *
     * @return the explosion force multiplier
     */
    public abstract double getExplosionForceMultiplier();
}
