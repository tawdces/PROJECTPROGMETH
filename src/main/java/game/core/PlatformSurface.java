package game.core;

import javafx.geometry.Rectangle2D;

public class PlatformSurface {

    private final Rectangle2D bounds;
    private final boolean oneWay;

    public PlatformSurface(double x, double y, double width, double height, boolean oneWay) {
        this.bounds = new Rectangle2D(x, y, width, height);
        this.oneWay = oneWay;
    }

    public Rectangle2D getBounds() {
        return bounds;
    }

    public boolean isOneWay() {
        return oneWay;
    }
}
