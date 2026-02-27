package game.entities;

import game.config.GameSettings;
import game.core.GameEntity;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet extends GameEntity {

    private final double velocityX;
    private final double velocityY;
    private final double impactForceX;
    private final double impactForceY;
    private final Player owner;

    public Bullet(
            double startX,
            double startY,
            double velocityX,
            double velocityY,
            double impactForceX,
            double impactForceY,
            Player owner
    ) {
        super(startX, startY, GameSettings.BULLET_SIZE, GameSettings.BULLET_SIZE);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.impactForceX = impactForceX;
        this.impactForceY = impactForceY;
        this.owner = owner;
    }

    @Override
    public void update(double deltaSeconds) {
        x += velocityX * deltaSeconds;
        y += velocityY * deltaSeconds;

        if (x < -80 || x > GameSettings.WIDTH + 80 || y < -80 || y > GameSettings.HEIGHT + 80) {
            deactivate();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillOval(x, y, width, height);
    }

    public Player getOwner() {
        return owner;
    }

    public double getImpactForceX() {
        return impactForceX;
    }

    public double getImpactForceY() {
        return impactForceY;
    }
}
