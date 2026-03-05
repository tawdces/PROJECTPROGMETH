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
    private static final Image EMPTY_GUN_SPRITE = new WritableImage(1, 1);

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
        this.sprite = new Image(AbstractGun.class.getResourceAsStream(spritePath));
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
        return createBullet(owner, muzzleX, muzzleY, facingDirection, verticalSpeed, Double.POSITIVE_INFINITY);
    }

    protected final Bullet createBullet(
            Player owner,
            double muzzleX,
            double muzzleY,
            int facingDirection,
            double verticalSpeed,
            double maxTravelDistance
    ) {
        return new Bullet(
                muzzleX,
                muzzleY,
                facingDirection * bulletSpeed,
                verticalSpeed,
                facingDirection * forceX,
                forceY,
                owner,
                maxTravelDistance
        );
    }
}
