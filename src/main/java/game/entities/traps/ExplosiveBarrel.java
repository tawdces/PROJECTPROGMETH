package game.entities.traps;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExplosiveBarrel extends Trap {
    private static final Image EMPTY_IMAGE = new WritableImage(1, 1);
    private static final Image BARREL_IMAGE =  new Image(ExplosiveBarrel.class.getResourceAsStream("/images/traps/Barrel.png"));

    public ExplosiveBarrel(double x, double y) {
        
        super(x, y - 42.0, 32.0, 42.0);
    }

    @Override
    public double getExplosionForceMultiplier() {
        
        return 1.0;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (BARREL_IMAGE != EMPTY_IMAGE) {
            gc.drawImage(BARREL_IMAGE, x, y, width, height);
        } else {
            gc.setFill(Color.web("#a32222"));
            gc.fillRoundRect(x, y, width, height, 6, 6);
            gc.setFill(Color.web("#333333"));
            gc.fillRect(x, y + 8, width, 4);
            gc.fillRect(x, y + 30, width, 4);
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Impact", FontWeight.NORMAL, 14));
            gc.fillText("TNT", x + 5, y + 25);
        }
    }
}