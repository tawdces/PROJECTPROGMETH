package game.entities.powerups;

import game.entities.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SpeedPowerUp extends PowerUp {

    public SpeedPowerUp(double x, double y) {
        super(x, y);
    }

    @Override
    public void applyEffect(Player player, long nowMillis) {

        player.applySpeedBoost(1.6, 5000, nowMillis);
    }

    @Override
    public void render(GraphicsContext gc) {

        gc.setFill(Color.web("#ffcc00", 0.85));
        gc.fillRoundRect(x, y, width, height, 8, 8);
        gc.setStroke(Color.web("#ffff88"));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x, y, width, height, 8, 8);


        gc.setFill(Color.web("#aa4400"));
        gc.setFont(Font.font("Impact", FontWeight.NORMAL, 18));
        gc.fillText(">>", x + 5, y + 19);
    }
}