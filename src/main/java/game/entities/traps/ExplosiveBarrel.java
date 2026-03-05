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

/**
 * Represents the explosive barrel.
 */
public class ExplosiveBarrel extends Trap {
    /**
     * Internal constant for drop gravity.
     */
    private static final double DROP_GRAVITY = 1_750.0;
    /**
     * Internal constant for drop max speed.
     */
    private static final double DROP_MAX_SPEED = 820.0;
    /**
     * Internal constant for empty image.
     */
    private static final Image EMPTY_IMAGE = new WritableImage(1, 1);
    /**
     * Internal constant for barrel image.
     */
    private static final Image BARREL_IMAGE = loadTransparentImage("/images/traps/Barrel.png");
    /**
     * Internal state field for landing y.
     */
    private final double landingY;
    /**
     * Internal state field for velocity y.
     */
    private double velocityY;
    /**
     * Internal state field for falling.
     */
    private boolean falling;

    /**
     * Creates a new explosive barrel instance.
     *
     * @param x parameter value
     * @param y parameter value
     */
    public ExplosiveBarrel(double x, double y) {
        super(x, y - 42.0, 32.0, 42.0);
        this.landingY = y - 42.0;
        this.falling = false;
    }

    /**
     * Creates a new explosive barrel instance.
     *
     * @param x parameter value
     * @param landingTopY parameter value
     * @param startTopY parameter value
     */
    public ExplosiveBarrel(double x, double landingTopY, double startTopY) {
        super(x, startTopY - 42.0, 32.0, 42.0);
        this.landingY = landingTopY - 42.0;
        this.falling = startTopY < landingTopY;
        if (!falling) {
            this.y = this.landingY;
        }
    }

    /**
     * Returns the explosion force multiplier.
     *
     * @return the explosion force multiplier
     */
    @Override
    public double getExplosionForceMultiplier() {

        return 1.0;
    }

    /**
     * Updates this object state for the current frame.
     *
     * @param deltaSeconds parameter value
     */
    @Override
    public void update(double deltaSeconds) {
        if (!falling) {
            return;
        }
        velocityY = Math.min(DROP_MAX_SPEED, velocityY + DROP_GRAVITY * deltaSeconds);
        y += velocityY * deltaSeconds;
        if (y >= landingY) {
            y = landingY;
            velocityY = 0.0;
            falling = false;
        }
    }

    /**
     * Renders this object.
     *
     * @param gc parameter value
     */
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

    /**
     * Internal helper for load transparent image.
     *
     * @param resourcePath parameter value
     * @return the resulting value
     */
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
