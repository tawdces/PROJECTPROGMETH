package game.entities;

import game.config.GameSettings;
import game.core.GameEntity;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class WeaponBox extends GameEntity {

    private static final double FALL_SPEED = 140.0;
    private final double stopY;
    private final WeaponType weaponType;

    public WeaponBox(double startX, double stopY, WeaponType weaponType) {
        super(startX, -GameSettings.BOX_SIZE, GameSettings.BOX_SIZE, GameSettings.BOX_SIZE);
        this.stopY = stopY;
        this.weaponType = weaponType;
    }

    @Override
    public void update(double deltaSeconds) {
        if (y < stopY) {
            y += FALL_SPEED * deltaSeconds;
            if (y > stopY) {
                y = stopY;
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isActive()) {
            return;
        }

        gc.setFill(weaponType.boxColor());
        gc.fillRect(x, y, width, height);

        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);

        gc.setFill(Color.BLACK);
        String tag = switch (weaponType) {
            case PISTOL -> "P";
            case RIFLE -> "R";
            case MACHINE_GUN -> "M";
            case SHOTGUN -> "S";
            default -> "G";
        };
        gc.fillText(tag, x + width / 2.0 - 4, y + height / 2.0 + 4);
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }
}
