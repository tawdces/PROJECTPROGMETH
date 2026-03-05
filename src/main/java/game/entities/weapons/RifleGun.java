package game.entities.weapons;

/**
 * Represents the rifle gun.
 */
public final class RifleGun extends AbstractGun {
    /**
     * Creates a new rifle gun instance.
     */
    public RifleGun() {
        super("Rifle", 15_000, 240, 860.0, 520.0, -35.0, 54.0, "/images/weapons/Rifle.png");
    }
}
