package game.entities.weapons;

import game.entities.Bullet;
import game.entities.Player;
import javafx.scene.image.Image;

import java.util.List;

public interface Gun {
    String label();

    long durationMillis();

    long cooldownMillis();

    Image sprite();

    double renderWidth();

    List<Bullet> fire(Player owner, double muzzleX, double muzzleY, int facingDirection);
}
