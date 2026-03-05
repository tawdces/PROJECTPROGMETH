package game.entities;

import game.logic.SpriteFrame;
import game.entities.Player;
import game.entities.powerups.PowerUp;
import game.testutil.FxTestUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class PowerUpTest {

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
    void constructor_setsFixedBoundsSize_andUpdateIsNoOp() {
        PowerUp p = new PowerUp(10, 20) {
            @Override
            public void applyEffect(Player player, long nowMillis) {
                // no-op for this test
            }

            @Override
            public void render(GraphicsContext gc) {
                // no-op for this test
            }
        };

        assertEquals(10.0, p.getBounds().getMinX(), 1e-9);
        assertEquals(20.0, p.getBounds().getMinY(), 1e-9);
        assertEquals(26.0, p.getBounds().getWidth(), 1e-9);
        assertEquals(26.0, p.getBounds().getHeight(), 1e-9);

        assertTrue(p.isActive());
        double x0 = p.getBounds().getMinX();
        double y0 = p.getBounds().getMinY();

        p.update(10.0); // PowerUp.update is a no-op by design

        assertTrue(p.isActive());
        assertEquals(x0, p.getBounds().getMinX(), 1e-9);
        assertEquals(y0, p.getBounds().getMinY(), 1e-9);
    }

    @Test
    void applyEffect_canBeInvoked_andReceivesPlayerAndTime() {
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicLong receivedNow = new AtomicLong(-1L);

        PowerUp p = new PowerUp(0, 0) {
            @Override
            public void applyEffect(Player player, long nowMillis) {
                assertNotNull(player);
                called.set(true);
                receivedNow.set(nowMillis);
            }

            @Override
            public void render(GraphicsContext gc) {
                // no-op
            }
        };

        long now = 123_456L;
        Player player = new TestPlayer(0, 0);

        assertDoesNotThrow(() -> p.applyEffect(player, now));
        assertTrue(called.get());
        assertEquals(now, receivedNow.get());
    }

    @Test
    void render_doesNotThrow_withGraphicsContext() {
        Canvas canvas = new Canvas(100, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        PowerUp p = new PowerUp(0, 0) {
            @Override
            public void applyEffect(Player player, long nowMillis) {
                // no-op
            }

            @Override
            public void render(GraphicsContext gc) {
                gc.setFill(Color.DEEPSKYBLUE);
                gc.fillRect(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
            }
        };

        assertDoesNotThrow(() -> p.render(gc));
    }

    @Test
    void deactivate_marksPowerUpInactive() {
        PowerUp p = new PowerUp(0, 0) {
            @Override
            public void applyEffect(Player player, long nowMillis) {
                // no-op
            }

            @Override
            public void render(GraphicsContext gc) {
                // no-op
            }
        };

        assertTrue(p.isActive());
        p.deactivate();
        assertFalse(p.isActive());
    }
}
