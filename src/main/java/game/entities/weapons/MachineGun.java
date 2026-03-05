package game.entities.weapons;

/**
 * Represents the machine gun.
 */
public final class MachineGun extends AbstractGun {
    /**
     * Creates a new machine gun instance.
     */
    public MachineGun() {
        super("Machine", 12_000, 120, 780.0, 360.0, -20.0, 58.0, "/images/weapons/Machine_gun.png");
    }
}
