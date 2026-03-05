package game.ui;

import game.config.GameSettings;
import game.entities.weapons.Gun;
import javafx.geometry.Rectangle2D;

record WeaponDrop(double x, double y, Gun gun) {
    Rectangle2D bounds() {
        return new Rectangle2D(x, y, GameSettings.BOX_SIZE, GameSettings.BOX_SIZE);
    }
}
