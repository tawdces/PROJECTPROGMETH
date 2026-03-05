package game.entities.weapons;

import game.entities.Bullet;
import game.entities.Player;
import javafx.scene.image.WritableImage;

import java.util.List;

/**
 * Represents the unarmed gun.
 */
public final class UnarmedGun implements Gun {
    /**
     * Internal constant for empty sprite.
     */
    private static final WritableImage EMPTY_SPRITE = new WritableImage(1, 1);

    /**
     * Creates the sentinel weapon representing an unarmed state.
     */
    public UnarmedGun() {
    }

    /**
     * Executes label.
     *
     * @return the resulting value
     */
    @Override
    public String label() {
        return "OFF";
    }

    /**
     * Executes duration millis.
     *
     * @return the resulting value
     */
    @Override
    public long durationMillis() {
        return 0L;
    }

    /**
     * Executes cooldown millis.
     *
     * @return the resulting value
     */
    @Override
    public long cooldownMillis() {
        return 0L;
    }

    /**
     * Executes sprite.
     *
     * @return the resulting value
     */
    @Override
    public javafx.scene.image.Image sprite() {
        return EMPTY_SPRITE;
    }

    /**
     * Executes render width.
     *
     * @return the resulting value
     */
    @Override
    public double renderWidth() {
        return 0.0;
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
        return List.of();
    }
}
