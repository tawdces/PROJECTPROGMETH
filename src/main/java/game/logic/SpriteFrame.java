package game.logic;

/**
 * Represents the sprite frame.
 *
 * @param x x-coordinate of the frame in the sprite sheet
 * @param y y-coordinate of the frame in the sprite sheet
 * @param width frame width in pixels
 * @param height frame height in pixels
 */
public record SpriteFrame(double x, double y, double width, double height) {
}
