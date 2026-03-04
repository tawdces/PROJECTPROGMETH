package game.entities.powerups;

import game.entities.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ShieldPowerUp extends PowerUp {

    public ShieldPowerUp(double x, double y) {
        super(x, y);
    }

    @Override
    public void applyEffect(Player player, long nowMillis) {
        
        player.applyShield(5000, nowMillis);
    }

    @Override
    public void render(GraphicsContext gc) {
        
        gc.setFill(Color.web("#2288ff", 0.85));
        gc.fillRoundRect(x, y, width, height, 8, 8);
        gc.setStroke(Color.web("#88ccff"));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x, y, width, height, 8, 8);
        
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Impact", FontWeight.NORMAL, 18));
        gc.fillText("S", x + 7, y + 19);
    }
}