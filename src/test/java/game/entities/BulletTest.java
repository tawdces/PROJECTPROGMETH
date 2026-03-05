package game.entities;

import game.config.GameSettings;
import game.logic.SpriteFrame;
import game.testutil.FxTestUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BulletTest {

    static final class TestPlayer extends Player {
        TestPlayer(double x, double y) {
            super(x, y, "T", Color.BLACK, 1, null, List.of(new SpriteFrame(0, 0, 1, 1)));
        }
    }

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @Test
    void getters_returnConstructorValues() {
        Player owner = new TestPlayer(10, 10);
        Bullet b = new Bullet(0, 0, 100, -50, 12.5, -7.5, owner);

        assertSame(owner, b.getOwner());
        assertEquals(12.5, b.getImpactForceX(), 1e-9);
        assertEquals(-7.5, b.getImpactForceY(), 1e-9);
    }

    @Test
    void update_movesBulletAndKeepsActiveWithinBounds() {
        Player owner = new TestPlayer(0, 0);
        Bullet b = new Bullet(10, 10, 100, 0, 0, 0, owner);

        double x0 = b.getBounds().getMinX();
        b.update(0.5); // 0.5s -> +50px in x

        assertTrue(b.isActive());
        assertEquals(x0 + 50.0, b.getBounds().getMinX(), 1e-6);
    }

    @Test
    void update_deactivatesWhenOutOfBlastZone() {
        Player owner = new TestPlayer(0, 0);

        double margin = GameSettings.BLAST_ZONE_MARGIN + 100.0;
        Bullet b = new Bullet(-margin - 1.0, 0, 0, 0, 0, 0, owner);

        assertTrue(b.isActive());
        b.update(0.016);
        assertFalse(b.isActive());
    }

    @Test
    void update_deactivatesWhenTravelDistanceExceedsMaxRange() {
        Player owner = new TestPlayer(0, 0);
        Bullet b = new Bullet(10, 10, 100, 0, 0, 0, owner, 50.0);

        b.update(0.49);
        assertTrue(b.isActive());

        b.update(0.02);
        assertFalse(b.isActive());
    }

    @Test
    void render_doesNotThrow() {
        Player owner = new TestPlayer(0, 0);
        Bullet b = new Bullet(10, 10, 100, 0, 0, 0, owner);

        Canvas canvas = new Canvas(200, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        assertDoesNotThrow(() -> b.render(gc));
    }
}
