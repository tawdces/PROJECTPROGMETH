package game.map;

import javafx.geometry.Rectangle2D;

/**
 * Represents the platform surface.
 */
public final class PlatformSurface {

    /**
     * Internal state field for bounds.
     */
    private final Rectangle2D bounds;
    /**
     * Internal state field for one way.
     */
    private final boolean oneWay;

    /**
     * Creates a new platform surface instance.
     *
     * @param x parameter value
     * @param y parameter value
     * @param width parameter value
     * @param height parameter value
     * @param oneWay parameter value
     */
    public PlatformSurface(double x, double y, double width, double height, boolean oneWay) {
        this.bounds = new Rectangle2D(x, y, width, height);
        this.oneWay = oneWay;
    }

    /**
     * Returns the bounds.
     *
     * @return the bounds
     */
    public Rectangle2D getBounds() {
        return bounds;
    }

    /**
     * Checks whether one way.
     *
     * @return true when the condition is met; otherwise false
     */
    public boolean isOneWay() {
        return oneWay;
    }
}
