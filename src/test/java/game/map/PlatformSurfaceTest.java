package game.map;

import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformSurfaceTest {

    @Test
    void constructor_setsBoundsAndOneWay() {
        PlatformSurface surface = new PlatformSurface(10, 20, 300, 40, true);

        Rectangle2D b = surface.getBounds();
        assertEquals(10, b.getMinX(), 1e-9);
        assertEquals(20, b.getMinY(), 1e-9);
        assertEquals(300, b.getWidth(), 1e-9);
        assertEquals(40, b.getHeight(), 1e-9);

        assertTrue(surface.isOneWay());
    }

    @Test
    void oneWay_falseWhenProvidedFalse() {
        PlatformSurface surface = new PlatformSurface(0, 0, 1, 1, false);
        assertFalse(surface.isOneWay());
    }
}
