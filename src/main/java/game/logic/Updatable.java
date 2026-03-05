package game.logic;

/**
 * Defines an object that updates its state every frame.
 */
public interface Updatable {
    /**
     * Advances the object state by one frame.
     *
     * @param deltaSeconds elapsed time since the previous frame, in seconds
     */
    void update(double deltaSeconds);
}
