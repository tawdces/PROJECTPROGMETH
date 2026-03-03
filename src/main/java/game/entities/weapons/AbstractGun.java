package game.entities.weapons;

import game.entities.Bullet;
import game.entities.Player;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;

public abstract class AbstractGun implements Gun {
    private final String label;
    private final long durationMillis;
    private final long cooldownMillis;
    private final double bulletSpeed;
    private final double forceX;
    private final double forceY;
    private final double renderWidth;
    private final Image sprite;

    protected AbstractGun(
            String label,
            long durationMillis,
            long cooldownMillis,
            double bulletSpeed,
            double forceX,
            double forceY,
            double renderWidth,
            String spritePath
    ) {
        this.label = label;
        this.durationMillis = durationMillis;
        this.cooldownMillis = cooldownMillis;
        this.bulletSpeed = bulletSpeed;
        this.forceX = forceX;
        this.forceY = forceY;
        this.renderWidth = renderWidth;
        this.sprite = loadTransparentSprite(spritePath);
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public long durationMillis() {
        return durationMillis;
    }

    @Override
    public long cooldownMillis() {
        return cooldownMillis;
    }

    @Override
    public Image sprite() {
        return sprite;
    }

    @Override
    public double renderWidth() {
        return renderWidth;
    }

    @Override
    public List<Bullet> fire(Player owner, double muzzleX, double muzzleY, int facingDirection) {
        return List.of(createBullet(owner, muzzleX, muzzleY, facingDirection, 0.0));
    }

    protected final Bullet createBullet(
            Player owner,
            double muzzleX,
            double muzzleY,
            int facingDirection,
            double verticalSpeed
    ) {
        return new Bullet(
                muzzleX,
                muzzleY,
                facingDirection * bulletSpeed,
                verticalSpeed,
                facingDirection * forceX,
                forceY,
                owner
        );
    }

    private static Image loadTransparentSprite(String resourcePath) {
        Image raw = new Image(Objects.requireNonNull(AbstractGun.class.getResourceAsStream(resourcePath)));
        int w = (int) raw.getWidth();
        int h = (int) raw.getHeight();
        WritableImage out = new WritableImage(w, h);
        PixelReader reader = raw.getPixelReader();
        PixelWriter writer = out.getPixelWriter();
        Color key = reader.getColor(0, 0);

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                Color c = reader.getColor(px, py);
                boolean transparentByKey = colorDistance(c, key) < 0.14;
                boolean transparentByWhite = c.getOpacity() > 0.0
                        && c.getRed() > 0.90
                        && c.getGreen() > 0.90
                        && c.getBlue() > 0.90;
                if (transparentByKey || transparentByWhite) {
                    writer.setColor(px, py, Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.0));
                } else {
                    writer.setColor(px, py, c);
                }
            }
        }
        return out;
    }

    private static double colorDistance(Color a, Color b) {
        double dr = a.getRed() - b.getRed();
        double dg = a.getGreen() - b.getGreen();
        double db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
