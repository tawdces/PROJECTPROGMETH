package game.entities.weapons;

import game.config.GameSettings;
import game.entities.Bullet;
import game.entities.Player;

import java.util.ArrayList;
import java.util.List;

public final class ShotgunGun extends AbstractGun {
    private static final double[] VERTICAL_SPREAD = {-190.0, -95.0, 0.0, 95.0, 190.0};

    public ShotgunGun() {
        super("Shotgun", 12_000, 520, 620.0, 255.0, -18.0, 64.0, "/images/weapons/Shotgun.png");
    }

    @Override
    public List<Bullet> fire(Player owner, double muzzleX, double muzzleY, int facingDirection) {
        List<Bullet> bullets = new ArrayList<>();
        for (double verticalSpeed : VERTICAL_SPREAD) {
            bullets.add(createBullet(
                    owner,
                    muzzleX,
                    muzzleY,
                    facingDirection,
                    verticalSpeed,
                    GameSettings.SHOTGUN_PELLET_RANGE
            ));
        }
        return bullets;
    }
}
