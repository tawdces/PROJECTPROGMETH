package game.logic;

import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CollidableTest {

    @Test
    void getBounds_returnsConfiguredRectangle() {
        Rectangle2D bounds = new Rectangle2D(12.0, 24.0, 30.0, 18.0);
        Collidable collidable = new TestCollidable(bounds);

        Rectangle2D actual = collidable.getBounds();

        assertSame(bounds, actual);
        assertEquals(12.0, actual.getMinX(), 0.000_001);
        assertEquals(24.0, actual.getMinY(), 0.000_001);
        assertEquals(30.0, actual.getWidth(), 0.000_001);
        assertEquals(18.0, actual.getHeight(), 0.000_001);
    }

    private static final class TestCollidable implements Collidable {
        private final Rectangle2D bounds;

        private TestCollidable(Rectangle2D bounds) {
            this.bounds = bounds;
        }

        @Override
        public Rectangle2D getBounds() {
            return bounds;
        }
    }
}
