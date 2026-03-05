package game.traps;

import game.testutil.FxTestUtils;
import game.entities.traps.ExplosiveBarrel;
import game.entities.traps.Landmine;
import game.entities.traps.Trap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrapTest {

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @Test
    void trapUpdate_doesNotMoveOrDeactivate() {
        Trap t = new Landmine(100, 200);

        double x0 = t.getBounds().getMinX();
        double y0 = t.getBounds().getMinY();

        assertTrue(t.isActive());
        t.update(10.0); // does nothing by design

        assertTrue(t.isActive(), "Trap.update() should not deactivate traps");
        assertEquals(x0, t.getBounds().getMinX(), 1e-9);
        assertEquals(y0, t.getBounds().getMinY(), 1e-9);
    }

    @Test
    void landmine_constructorOffsetsY_andExplosionMultiplierInRange() {
        double x = 50;
        double y = 80;

        Landmine m = new Landmine(x, y);

        assertEquals(x, m.getBounds().getMinX(), 1e-9);
        assertEquals(y - 8.0, m.getBounds().getMinY(), 1e-9);
        assertEquals(24.0, m.getBounds().getWidth(), 1e-9);
        assertEquals(8.0, m.getBounds().getHeight(), 1e-9);

        double mult = m.getExplosionForceMultiplier();
        assertTrue(mult >= 0.5 && mult <= 2.0, "Landmine multiplier should be in [0.5, 2.0]");
    }

    @Test
    void explosiveBarrel_constructorOffsetsY_andExplosionMultiplierIsOne() {
        double x = 10;
        double y = 70;

        ExplosiveBarrel b = new ExplosiveBarrel(x, y);

        assertEquals(x, b.getBounds().getMinX(), 1e-9);
        assertEquals(y - 42.0, b.getBounds().getMinY(), 1e-9);
        assertEquals(32.0, b.getBounds().getWidth(), 1e-9);
        assertEquals(42.0, b.getBounds().getHeight(), 1e-9);

        assertEquals(1.0, b.getExplosionForceMultiplier(), 1e-12);
    }

    @Test
    void render_landmineAndBarrel_doNotThrow() {
        Canvas canvas = new Canvas(200, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Landmine m = new Landmine(20, 60);
        ExplosiveBarrel b = new ExplosiveBarrel(80, 120);

        assertDoesNotThrow(() -> m.render(gc));
        assertDoesNotThrow(() -> b.render(gc));
    }

    @Test
    void deactivate_marksTrapInactive() {
        Trap t = new ExplosiveBarrel(0, 50);
        assertTrue(t.isActive());

        t.deactivate();
        assertFalse(t.isActive());
    }
}
