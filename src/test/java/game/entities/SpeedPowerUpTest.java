package game.entities;

import game.core.SpriteFrame;
import game.entities.Player;
import game.entities.powerups.SpeedPowerUp;
import game.testutil.FxTestUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SpeedPowerUpTest {

    static final class SpyPlayer extends Player {
        final AtomicReference<Double> speedMultiplier = new AtomicReference<>(null);
        final AtomicLong boostDuration = new AtomicLong(-1);
        final AtomicLong boostNow = new AtomicLong(-1);

        SpyPlayer(double x, double y) {
            super(x, y, "T", Color.BLACK, 1, null, List.of(new SpriteFrame(0, 0, 1, 1)));
        }

        @Override
        public void applySpeedBoost(double multiplier, long durationMillis, long nowMillis) {
            speedMultiplier.set(multiplier);
            boostDuration.set(durationMillis);
            boostNow.set(nowMillis);
        }
    }

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @Test
    void applyEffect_callsPlayerApplySpeedBoost_withExpectedValues() {
        SpeedPowerUp p = new SpeedPowerUp(10, 20);
        SpyPlayer player = new SpyPlayer(0, 0);

        long now = 987_654L;
        p.applyEffect(player, now);

        assertNotNull(player.speedMultiplier.get());
        assertEquals(1.6, player.speedMultiplier.get(), 1e-12, "SpeedPowerUp should apply multiplier 1.6");
        assertEquals(5_000L, player.boostDuration.get(), "SpeedPowerUp should apply a 5000ms boost");
        assertEquals(now, player.boostNow.get(), "SpeedPowerUp should pass through nowMillis");
    }

    @Test
    void render_doesNotThrow() {
        SpeedPowerUp p = new SpeedPowerUp(10, 20);

        Canvas canvas = new Canvas(120, 120);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        assertDoesNotThrow(() -> p.render(gc));
    }
}
