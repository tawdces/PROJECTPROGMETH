package game.entities.weapons;

import java.util.List;

/**
 * Represents the gun registry.
 */
public final class GunRegistry {
    /**
     * Constant for unarmed.
     */
    public static final Gun UNARMED = new UnarmedGun();
    /**
     * Constant for pistol.
     */
    public static final Gun PISTOL = new PistolGun();
    /**
     * Constant for rifle.
     */
    public static final Gun RIFLE = new RifleGun();
    /**
     * Constant for machine gun.
     */
    public static final Gun MACHINE_GUN = new MachineGun();
    /**
     * Constant for shotgun.
     */
    public static final Gun SHOTGUN = new ShotgunGun();
    /**
     * Constant for selectable guns.
     */
    public static final List<Gun> SELECTABLE_GUNS = List.of(PISTOL, RIFLE, MACHINE_GUN, SHOTGUN);

    /**
     * Creates a private gun registry instance.
     */
    private GunRegistry() {
    }
}
