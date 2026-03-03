package game.entities.weapons;

import java.util.List;

public final class GunRegistry {
    public static final Gun UNARMED = new UnarmedGun();
    public static final Gun PISTOL = new PistolGun();
    public static final Gun RIFLE = new RifleGun();
    public static final Gun MACHINE_GUN = new MachineGun();
    public static final Gun SHOTGUN = new ShotgunGun();
    public static final List<Gun> SELECTABLE_GUNS = List.of(PISTOL, RIFLE, MACHINE_GUN, SHOTGUN);

    private GunRegistry() {
    }
}
