package game.ui;

import javafx.scene.image.Image;

record GameEffect(
        String type,
        Image image,
        double x,
        double y,
        double width,
        double height,
        long expiresAt,
        long totalLife
) {
    boolean isExpired(long nowMillis) {
        return expiresAt <= nowMillis;
    }
}
