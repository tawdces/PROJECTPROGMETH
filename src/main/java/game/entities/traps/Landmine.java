package game.entities.traps;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Landmine extends Trap {
    private final long spawnTime;
    private final double explosionMultiplier;

    public Landmine(double x, double y) {

        super(x, y - 8.0, 24.0, 8.0);
        this.spawnTime = System.currentTimeMillis();


        this.explosionMultiplier = 0.5 + (Math.random() * 1.5);
    }

    @Override
    public double getExplosionForceMultiplier() {
        return explosionMultiplier;
    }

    @Override
    public void render(GraphicsContext gc) {

        gc.setFill(Color.web("#2a2a2a"));
        gc.fillOval(x, y, width, height);


        long now = System.currentTimeMillis();
        long blinkSpeed = explosionMultiplier > 1.5 ? 100L : 300L;
        boolean blink = ((now - spawnTime) / blinkSpeed) % 2L == 0L;

        gc.setFill(blink ? Color.web("#ff2222") : Color.web("#880000"));
        gc.fillOval(x + width / 2 - 3, y + 1, 6, 6);
    }
}