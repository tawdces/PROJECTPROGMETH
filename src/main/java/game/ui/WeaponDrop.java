package game.ui;

import game.config.GameSettings;
import game.entities.weapons.Gun;
import javafx.geometry.Rectangle2D;

final class WeaponDrop {
    private static final double DROP_GRAVITY = 1_600.0;
    private static final double DROP_MAX_SPEED = 780.0;

    private final double x;
    private final Gun gun;
    private final double landingY;

    private double y;
    private double velocityY;
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
