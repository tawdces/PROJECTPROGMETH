package game.entities;

import game.logic.SpriteFrame;
import javafx.scene.paint.Color;

import java.util.List;

public final class PlayerTwo extends Player {
    public PlayerTwo(double startX, double startY) {
        super(startX, startY, "P2", Color.CRIMSON, -1, "/images/players/Player2.png", List.of());
    }

    public PlayerTwo(double startX, double startY, String spriteResourcePath, List<SpriteFrame> spriteFrames) {
        super(startX, startY, "P2", Color.CRIMSON, -1, spriteResourcePath, spriteFrames);
    }
}
