package game.logic;

import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedMultiplayerCameraTest {

    @Test
    void snapToTargets_computesCenterAndZoomFromAllPlayers() {
        SharedMultiplayerCamera camera = new SharedMultiplayerCamera(
                1000, 500,
                -5000, 5000,
                -5000, 5000,
                0.5, 2.0,
                100, 50,
                8.0
        );

        List<Rectangle2D> players = List.of(
                new Rectangle2D(0, 0, 50, 50),
                new Rectangle2D(450, 100, 50, 50)
        );

        camera.snapToTargets(players);

        assertEquals(250.0, camera.getCameraX() + (camera.getViewWidth() * 0.5), 0.001);
        assertEquals(75.0, camera.getCameraY() + (camera.getViewHeight() * 0.5), 0.001);
        assertEquals(1000.0 / 700.0, camera.getZoom(), 0.001);
    }

    @Test
    void zoom_isClampedBetweenMinAndMax() {
        SharedMultiplayerCamera camera = new SharedMultiplayerCamera(
                1000, 500,
                -10000, 10000,
                -10000, 10000,
                0.5, 1.2,
                0, 0,
                8.0
        );

        camera.snapToTargets(List.of(
                new Rectangle2D(100, 100, 5, 5),
                new Rectangle2D(106, 102, 5, 5)
        ));
        assertEquals(1.2, camera.getZoom(), 0.0001);

        camera.snapToTargets(List.of(
                new Rectangle2D(-2000, 0, 10, 10),
                new Rectangle2D(2000, 0, 10, 10)
        ));
        assertEquals(0.5, camera.getZoom(), 0.0001);
    }

    @Test
    void supportsUpToFourPlayers_andRespectsWorldBounds() {
        SharedMultiplayerCamera camera = new SharedMultiplayerCamera(
                400, 300,
                0, 1000,
                0, 800,
                0.8, 2.0,
                40, 40,
                8.0
        );

        camera.snapToTargets(List.of(
                new Rectangle2D(0, 20, 30, 30),
                new Rectangle2D(60, 25, 30, 30),
                new Rectangle2D(80, 30, 30, 30),
                new Rectangle2D(110, 35, 30, 30)
        ));

        assertTrue(camera.getCameraX() >= 0.0);
        assertTrue(camera.getCameraY() >= 0.0);
    }

    @Test
    void update_movesSmoothlyTowardTargetWithoutSnapping() {
        SharedMultiplayerCamera camera = new SharedMultiplayerCamera(
                800, 450,
                -5000, 5000,
                -5000, 5000,
                0.7, 2.0,
                100, 100,
                5.0
        );

        List<Rectangle2D> start = List.of(
                new Rectangle2D(0, 0, 40, 40),
                new Rectangle2D(200, 0, 40, 40)
        );
        List<Rectangle2D> moved = List.of(
                new Rectangle2D(800, 300, 40, 40),
                new Rectangle2D(1200, 300, 40, 40)
        );

        camera.snapToTargets(start);
        double beforeX = camera.getCameraX();

        SharedMultiplayerCamera targetCamera = new SharedMultiplayerCamera(
                800, 450,
                -5000, 5000,
                -5000, 5000,
                0.7, 2.0,
                100, 100,
                5.0
        );
        targetCamera.snapToTargets(moved);
        double finalTargetX = targetCamera.getCameraX();

        camera.update(0.016, moved);
        double afterX = camera.getCameraX();

        assertNotEquals(beforeX, afterX);
        assertTrue(Math.abs(finalTargetX - afterX) < Math.abs(finalTargetX - beforeX));
    }
}
