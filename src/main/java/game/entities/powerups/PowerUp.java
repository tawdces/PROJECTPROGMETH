package game.entities.powerups;

import game.entities.GameEntity;
import game.entities.Player;
import javafx.scene.canvas.GraphicsContext;

/**
 * Base type for collectible power-ups that apply temporary player effects.
 */
public abstract class PowerUp extends GameEntity {

    /**
     * Creates a power-up at the given location.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    protected PowerUp(double x, double y) {

        super(x, y, 26, 26);
    }

    /**
     * Updates this object state for the current frame.
     *
     * @param deltaSeconds parameter value
     */
    @Override
    public void update(double deltaSeconds) {

    }


    /**
     * Applies effect.
     *
     * @param player parameter value
     * @param nowMillis parameter value
     */
    public abstract void applyEffect(Player player, long nowMillis);


    /**
     * Renders this object.
     *
     * @param gc parameter value
     */
    public abstract void render(GraphicsContext gc);
}
