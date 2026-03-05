package game.ui;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class GameImageLoader {
    /**
     * Internal constant for empty image.
     */
    private static final Image EMPTY_IMAGE = new WritableImage(1, 1);

    /**
     * Creates a private game image loader instance.
     */
    private GameImageLoader() {
    }

    static Image emptyImage() {
        return EMPTY_IMAGE;
    }

    static Image loadTransparentImage(Class<?> ownerClass, String resourcePath) {
        return loadImageInternal(ownerClass, resourcePath, true);
    }

    /**
     * Internal helper for load image internal.
     *
     * @param ownerClass parameter value
     * @param resourcePath parameter value
     * @param transparent parameter value
     * @return the resulting value
     */
    private static Image loadImageInternal(Class<?> ownerClass, String resourcePath, boolean transparent) {
        Image image;
        var url = ownerClass.getResource(resourcePath);
        if (url != null) {
            image = new Image(url.toExternalForm(), false);
            if (image.isError()) {
                return EMPTY_IMAGE;
            }
            return transparent ? makeBackgroundTransparent(image) : image;
        }

        Path fallback = Paths.get("src", "main", "resources", resourcePath.replaceFirst("^/", ""));
        if (Files.exists(fallback)) {
            image = new Image(fallback.toUri().toString(), false);
            if (image.isError()) {
                return EMPTY_IMAGE;
            }
            return transparent ? makeBackgroundTransparent(image) : image;
        }
        return EMPTY_IMAGE;
    }

    /**
     * Internal helper for make background transparent.
     *
     * @param raw parameter value
     * @return the resulting value
     */
    private static Image makeBackgroundTransparent(Image raw) {
        if (raw == null || raw.isError() || raw == EMPTY_IMAGE) {
            return EMPTY_IMAGE;
        }
        int width = (int) Math.round(raw.getWidth());
        int height = (int) Math.round(raw.getHeight());
        if (width <= 0 || height <= 0) {
            return EMPTY_IMAGE;
        }

        PixelReader reader = raw.getPixelReader();
        if (reader == null) {
            return EMPTY_IMAGE;
        }

        WritableImage out = new WritableImage(width, height);
        PixelWriter writer = out.getPixelWriter();
        Color key = reader.getColor(0, 0);

        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                Color color = reader.getColor(px, py);
                boolean transparentByKey = colorDistance(color, key) < 0.18;
                boolean transparentByWhite = color.getOpacity() > 0.0
                        && color.getBrightness() > 0.94
                        && color.getSaturation() < 0.16;
                if (transparentByKey || transparentByWhite) {
                    writer.setColor(px, py, Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.0));
                } else {
                    writer.setColor(px, py, color);
                }
            }
        }
        return out;
    }

    /**
     * Internal helper for color distance.
     *
     * @param a parameter value
     * @param b parameter value
     * @return the resulting value
     */
    private static double colorDistance(Color a, Color b) {
        double dr = a.getRed() - b.getRed();
        double dg = a.getGreen() - b.getGreen();
        double db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
