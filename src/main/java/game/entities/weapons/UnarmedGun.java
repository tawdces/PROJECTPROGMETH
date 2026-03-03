package game.entities.weapons;

import game.entities.Bullet;
import game.entities.Player;
import javafx.scene.image.WritableImage;

import java.util.List;

public final class UnarmedGun implements Gun {
    private static final WritableImage EMPTY_SPRITE = new WritableImage(1, 1);

    @Override
    public String label() {
        return "OFF";
    }

    @Override
    public long durationMillis() {
        return 0L;
    }

    @Override
    public long cooldownMillis() {
        return 0L;
    }

    @Override
    public javafx.scene.image.Image sprite() {
        return EMPTY_SPRITE;
    }

    @Override
    public double renderWidth() {
        return 0.0;
    }

    @Override
    public List<Bullet> fire(Player owner, double muzzleX, double muzzleY, int facingDirection) {
        return List.of();
    }
}
