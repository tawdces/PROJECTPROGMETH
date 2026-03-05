package game.entities;

import game.logic.SpriteFrame;
import game.entities.Player;
import game.entities.powerups.ShieldPowerUp;
import game.testutil.FxTestUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ShieldPowerUpTest {

    static final class SpyPlayer extends Player {
        final AtomicLong shieldDuration = new AtomicLong(-1);
        final AtomicLong shieldNow = new AtomicLong(-1);

        SpyPlayer(double x, double y) {
            super(x, y, "T", Color.BLACK, 1, null, List.of(new SpriteFrame(0, 0, 1, 1)));
        }

        @Override
        public void applyShield(long durationMillis, long nowMillis) {
            shieldDuration.set(durationMillis);
            shieldNow.set(nowMillis);
        }
    }

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @Test
    void applyEffect_callsPlayerApplyShield_withExpectedValues() {
        ShieldPowerUp p = new ShieldPowerUp(10, 20);
        SpyPlayer player = new SpyPlayer(0, 0);

        long now = 123_456L;
        p.applyEffect(player, now);

        assertEquals(5_000L, player.shieldDuration.get(), "ShieldPowerUp should apply a 5000ms shield");
        assertEquals(now, player.shieldNow.get(), "ShieldPowerUp should pass through nowMillis");
    }

    @Test
    void render_doesNotThrow() {
        ShieldPowerUp p = new ShieldPowerUp(10, 20);

        Canvas canvas = new Canvas(120, 120);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        assertDoesNotThrow(() -> p.render(gc));
    }
}
