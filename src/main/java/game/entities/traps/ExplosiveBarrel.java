package game.entities.traps;

import game.config.GameSettings;
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
    private static final Image BARREL_IMAGE = loadTransparentImage("/images/traps/Barrel.png");

    public ExplosiveBarrel(double x, double y) {
        super(
                x,
                y - GameSettings.BARREL_SIZE,
                GameSettings.BARREL_SIZE,
                GameSettings.BARREL_SIZE
        );
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

    private static Image loadTransparentImage(String resourcePath) {
        try {
            Image image;
            var url = ExplosiveBarrel.class.getResource(resourcePath);
            if (url != null) {
                image = new Image(url.toExternalForm(), false);
            } else {
                Path fallback = Paths.get("src", "main", "resources", resourcePath.replaceFirst("^/", ""));
                if (Files.exists(fallback)) {
                    image = new Image(fallback.toUri().toString(), false);
                } else {
                    return EMPTY_IMAGE;
                }
            }

            if (image.isError()) return EMPTY_IMAGE;

            int w = (int) Math.round(image.getWidth());
            int h = (int) Math.round(image.getHeight());
            if (w <= 0 || h <= 0) return EMPTY_IMAGE;

            PixelReader reader = image.getPixelReader();
            if (reader == null) return EMPTY_IMAGE;

            WritableImage out = new WritableImage(w, h);
            PixelWriter writer = out.getPixelWriter();
            Color key = reader.getColor(0, 0);

            for (int py = 0; py < h; py++) {
                for (int px = 0; px < w; px++) {
                    Color c = reader.getColor(px, py);
                    double dr = c.getRed() - key.getRed();
                    double dg = c.getGreen() - key.getGreen();
                    double db = c.getBlue() - key.getBlue();
                    boolean transparentByKey = Math.sqrt(dr * dr + dg * dg + db * db) < 0.18;
                    boolean transparentByWhite = c.getOpacity() > 0.0 && c.getBrightness() > 0.94 && c.getSaturation() < 0.16;

                    if (transparentByKey || transparentByWhite) {
                        writer.setColor(px, py, Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.0));
                    } else {
                        writer.setColor(px, py, c);
                    }
                }
            }
            return out;
        } catch (Exception e) {
            return EMPTY_IMAGE;
        }
    }
}
