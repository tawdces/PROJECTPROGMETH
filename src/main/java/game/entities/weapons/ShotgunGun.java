package game.entities.weapons;

import game.entities.Bullet;
import game.entities.Player;

import java.util.ArrayList;
import java.util.List;

public final class ShotgunGun extends AbstractGun {
    private static final double[] VERTICAL_SPREAD = {-120.0, -60.0, 0.0, 60.0, 120.0};

    public ShotgunGun() {
        super("Shotgun", 14_000, 600, 560.0, 460.0, -35.0, 62.0, "/Shotgun.png");
    }

    @Override
    public List<Bullet> fire(Player owner, double muzzleX, double muzzleY, int facingDirection) {
        List<Bullet> bullets = new ArrayList<>();
        for (double verticalSpeed : VERTICAL_SPREAD) {
            bullets.add(createBullet(owner, muzzleX, muzzleY, facingDirection, verticalSpeed));
        }
        return bullets;
    }
}
