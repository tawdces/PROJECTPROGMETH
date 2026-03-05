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

/**
 * Base implementation for guns that fire projectile bullets.
 */
public abstract class AbstractGun implements Gun {
    /**
     * Internal constant for empty gun sprite.
     */
    private static final Image EMPTY_GUN_SPRITE = new WritableImage(1, 1);

    /**
     * Internal state field for label.
     */
    private final String label;
    /**
     * Internal state field for duration millis.
     */
    private final long durationMillis;
    /**
     * Internal state field for cooldown millis.
     */
    private final long cooldownMillis;
    /**
     * Internal state field for bullet speed.
     */
    private final double bulletSpeed;
    /**
     * Internal state field for force x.
     */
    private final double forceX;
    /**
     * Internal state field for force y.
     */
    private final double forceY;
    /**
     * Internal state field for render width.
     */
    private final double renderWidth;
    /**
     * Internal state field for sprite.
     */
    private final Image sprite;

    /**
     * Creates a gun with shared stats and rendering metadata.
     *
     * @param label display name used in the UI
     * @param durationMillis how long the weapon stays equipped after pickup
     * @param cooldownMillis delay between consecutive shots
     * @param bulletSpeed horizontal bullet speed
     * @param forceX horizontal knockback force from bullet hits
     * @param forceY vertical knockback force from bullet hits
     * @param renderWidth preferred sprite render width
     * @param spritePath classpath resource path to the gun sprite
     */
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

    /**
     * Executes label.
     *
     * @return the resulting value
     */
    @Override
    public String label() {
        return label;
    }

    /**
     * Executes duration millis.
     *
     * @return the resulting value
     */
    @Override
    public long durationMillis() {
        return durationMillis;
    }

    /**
     * Executes cooldown millis.
     *
     * @return the resulting value
     */
    @Override
    public long cooldownMillis() {
        return cooldownMillis;
    }

    /**
     * Executes sprite.
     *
     * @return the resulting value
     */
    @Override
    public Image sprite() {
        return sprite;
    }

    /**
     * Executes render width.
     *
     * @return the resulting value
     */
    @Override
    public double renderWidth() {
        return renderWidth;
    }

    /**
     * Executes fire.
     *
     * @param owner parameter value
     * @param muzzleX parameter value
     * @param muzzleY parameter value
     * @param facingDirection parameter value
     * @return the resulting value
     */
    @Override
    public List<Bullet> fire(Player owner, double muzzleX, double muzzleY, int facingDirection) {
        return List.of(createBullet(owner, muzzleX, muzzleY, facingDirection, 0.0));
    }

    /**
     * Creates a bullet with optional vertical speed and unlimited range.
     *
     * @param owner player that fired the bullet
     * @param muzzleX spawn x-coordinate
     * @param muzzleY spawn y-coordinate
     * @param facingDirection horizontal direction ({@code -1} or {@code 1})
     * @param verticalSpeed vertical bullet speed
     * @return created bullet instance
     */
    protected final Bullet createBullet(
            Player owner,
            double muzzleX,
            double muzzleY,
            int facingDirection,
            double verticalSpeed
    ) {
        return createBullet(owner, muzzleX, muzzleY, facingDirection, verticalSpeed, Double.POSITIVE_INFINITY);
    }

    /**
     * Creates a bullet with optional vertical speed and a maximum travel distance.
     *
     * @param owner player that fired the bullet
     * @param muzzleX spawn x-coordinate
     * @param muzzleY spawn y-coordinate
     * @param facingDirection horizontal direction ({@code -1} or {@code 1})
     * @param verticalSpeed vertical bullet speed
     * @param maxTravelDistance maximum distance the bullet can travel
     * @return created bullet instance
     */
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
