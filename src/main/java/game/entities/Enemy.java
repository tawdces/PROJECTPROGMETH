package game.entities;

import game.core.SpriteFrame;
import javafx.scene.paint.Color;
import java.util.List;

public class Enemy extends Player {

    public Enemy(double startX, double startY) {
        super(startX, startY, "P2", Color.CRIMSON, -1);
    }

    public Enemy(double startX, double startY, String spriteResourcePath, List<SpriteFrame> spriteFrames) {
        super(startX, startY, "P2", Color.CRIMSON, -1, spriteResourcePath, spriteFrames);
    }
}
