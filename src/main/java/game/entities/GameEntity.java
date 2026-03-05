package game.entities;

import game.logic.Collidable;
import game.logic.Renderable;
import game.logic.Updatable;
import javafx.geometry.Rectangle2D;

public abstract class GameEntity implements Updatable, Renderable, Collidable {

    protected double x;
    protected double y;
    protected final double width;
    protected final double height;
    private boolean active = true;

    protected GameEntity(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
