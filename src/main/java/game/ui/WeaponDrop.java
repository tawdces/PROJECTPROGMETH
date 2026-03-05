package game.ui;

import game.config.GameSettings;
import game.entities.weapons.Gun;
import javafx.geometry.Rectangle2D;

final class WeaponDrop {
    /**
     * Internal constant for drop gravity.
     */
    private static final double DROP_GRAVITY = 1_600.0;
    /**
     * Internal constant for drop max speed.
     */
    private static final double DROP_MAX_SPEED = 780.0;

    /**
     * Internal state field for x.
     */
    private final double x;
    /**
     * Internal state field for gun.
     */
    private final Gun gun;
    /**
     * Internal state field for landing y.
     */
    private final double landingY;

    /**
     * Internal state field for y.
     */
    private double y;
    /**
     * Internal state field for velocity y.
     */
    private double velocityY;
    /**
     * Internal state field for landed.
     */
    private boolean landed;

    WeaponDrop(double x, double y, Gun gun) {
        this(x, y, y, gun);
    }

    WeaponDrop(double x, double startY, double landingY, Gun gun) {
        this.x = x;
        this.y = startY;
        this.landingY = landingY;
        this.gun = gun;
        this.landed = startY >= landingY;
        if (this.landed) {
            this.y = landingY;
        }
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    Gun gun() {
        return gun;
    }

    boolean isLanded() {
        return landed;
    }

    void update(double deltaSeconds) {
        if (landed) {
            return;
        }
        velocityY = Math.min(DROP_MAX_SPEED, velocityY + DROP_GRAVITY * deltaSeconds);
        y += velocityY * deltaSeconds;
        if (y >= landingY) {
            y = landingY;
            velocityY = 0.0;
            landed = true;
        }
    }

    Rectangle2D bounds() {
        return new Rectangle2D(x, y, GameSettings.BOX_SIZE, GameSettings.BOX_SIZE);
    }
}
