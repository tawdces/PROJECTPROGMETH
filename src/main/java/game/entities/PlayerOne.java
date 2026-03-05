package game.entities;

import game.logic.SpriteFrame;
import javafx.scene.paint.Color;

import java.util.List;

public final class PlayerOne extends Player {
    public PlayerOne(double startX, double startY) {
        super(startX, startY, "P1", Color.DODGERBLUE, 1, "/Player1.png", List.of());
    }

    public PlayerOne(double startX, double startY, String spriteResourcePath, List<SpriteFrame> spriteFrames) {
        super(startX, startY, "P1", Color.DODGERBLUE, 1, spriteResourcePath, spriteFrames);
    }
}
