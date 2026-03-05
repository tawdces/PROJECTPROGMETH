package game.entities;

import game.config.GameSettings;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Objects;

public class Bullet extends GameEntity {
    private static final Image EMPTY_BULLET_SPRITE = new WritableImage(1, 1);
    private static final Image BULLET_MOVE_IMAGE = new Image(Bullet.class.getResourceAsStream("/images/effects/Bullet_move.png"));

    private final double velocityX;
    private final double velocityY;
    private final double impactForceX;
    private final double impactForceY;
    private final Player owner;
    private final double maxTravelDistance;
    private double traveledDistance;

    public Bullet(
            double startX,
            double startY,
            double velocityX,
            double velocityY,
            double impactForceX,
            double impactForceY,
            Player owner
    ) {
        this(startX, startY, velocityX, velocityY, impactForceX, impactForceY, owner, Double.POSITIVE_INFINITY);
    }

    public Bullet(
            double startX,
            double startY,
            double velocityX,
            double velocityY,
            double impactForceX,
            double impactForceY,
            Player owner,
            double maxTravelDistance
    ) {
        super(startX, startY, GameSettings.BULLET_SIZE, GameSettings.BULLET_SIZE);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.impactForceX = impactForceX;
        this.impactForceY = impactForceY;
        this.owner = owner;
        this.maxTravelDistance = maxTravelDistance;
    }

    @Override
    public void update(double deltaSeconds) {
        double stepX = velocityX * deltaSeconds;
        double stepY = velocityY * deltaSeconds;
        x += stepX;
        y += stepY;
        traveledDistance += Math.hypot(stepX, stepY);

        if (Double.isFinite(maxTravelDistance) && traveledDistance >= maxTravelDistance) {
            deactivate();
            return;
        }

        double margin = GameSettings.BLAST_ZONE_MARGIN + 100.0;
        if (x < -margin || x > GameSettings.WIDTH + margin || y < -margin || y > GameSettings.HEIGHT + margin) {
            deactivate();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        double centerX = x + (width * 0.5);
        double centerY = y + (height * 0.5);
        double angleDeg = Math.toDegrees(Math.atan2(velocityY, velocityX));
        double drawWidth = width * 1.85;
        double drawHeight = Math.max(4.0, height * 0.90);

        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(angleDeg);
        gc.drawImage(BULLET_MOVE_IMAGE, -drawWidth * 0.5, -drawHeight * 0.5, drawWidth, drawHeight);
        gc.restore();

        gc.setStroke(Color.web("#fff2b5", 0.55));
        gc.setLineWidth(1.1);
        gc.strokeLine(centerX - (velocityX * 0.012), centerY - (velocityY * 0.012), centerX, centerY);
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

   
    private static double colorDistance(Color a, Color b) {
        double dr = a.getRed() - b.getRed();
        double dg = a.getGreen() - b.getGreen();
        double db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
