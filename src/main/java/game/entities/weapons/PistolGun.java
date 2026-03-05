package game.entities.weapons;

/**
 * Represents the pistol gun.
 */
public final class PistolGun extends AbstractGun {
    /**
     * Creates a new pistol gun instance.
     */
    public PistolGun() {
        super("Pistol", 15_000, 290, 740.0, 610.0, -45.0, 50.0, "/images/weapons/Pistol.png");
    }
}
