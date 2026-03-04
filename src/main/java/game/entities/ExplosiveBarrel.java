package game.entities;

import game.config.GameSettings;
import game.core.GameEntity;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ExplosiveBarrel extends GameEntity {

    private final Image image;

    public ExplosiveBarrel(double x, double y, Image image) {
        super(x, y, GameSettings.BARREL_SIZE, GameSettings.BARREL_SIZE);
        this.image = image;
    }

    @Override
    public void update(double deltaSeconds) {
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(image, x, y, width, height);
    }
}