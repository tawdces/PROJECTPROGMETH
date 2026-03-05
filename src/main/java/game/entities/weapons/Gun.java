package game.entities.weapons;

import game.entities.Bullet;
import game.entities.Player;
import javafx.scene.image.Image;

import java.util.List;

/**
 * Describes a weapon that a player can equip and fire.
 */
public interface Gun {
    /**
     * Returns the display name shown in the UI.
     *
     * @return weapon display name
     */
    String label();

    /**
     * Returns how long this weapon stays equipped after pickup.
     *
     * @return duration in milliseconds
     */
    long durationMillis();

    /**
     * Returns the minimum delay between consecutive shots.
     *
     * @return cooldown duration in milliseconds
     */
    long cooldownMillis();

    /**
     * Returns the sprite used to render this weapon.
     *
     * @return weapon sprite image
     */
    Image sprite();

    /**
     * Returns the preferred render width for this weapon sprite.
     *
     * @return sprite width in pixels
     */
    double renderWidth();

    /**
     * Fires the weapon and produces the bullets for this shot.
     *
     * @param owner player who fired the weapon
     * @param muzzleX projectile spawn x position
     * @param muzzleY projectile spawn y position
     * @param facingDirection horizontal firing direction ({@code -1} or {@code 1})
     * @return bullets created by this shot
     */
    List<Bullet> fire(Player owner, double muzzleX, double muzzleY, int facingDirection);
}
