package game.entities.powerups;

import game.core.GameEntity;
import game.entities.Player;
import javafx.scene.canvas.GraphicsContext;

public abstract class PowerUp extends GameEntity {

    protected PowerUp(double x, double y) {
        
        super(x, y, 26, 26);
    }

    @Override
    public void update(double deltaSeconds) {
        
    }

    
    public abstract void applyEffect(Player player, long nowMillis);
    
    
    public abstract void render(GraphicsContext gc);
}