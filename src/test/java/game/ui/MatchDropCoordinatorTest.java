package game.ui;

import game.config.GameSettings;
import game.entities.traps.ExplosiveBarrel;
import game.entities.traps.Trap;
import game.map.PlatformSurface;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MatchDropCoordinatorTest {

    private static final class FixedRandom extends Random {
        @Override
        public int nextInt(int bound) {
            return 0;
        }

        @Override
        public double nextDouble() {
            return 0.5;
        }

        @Override
        public boolean nextBoolean() {
            return true;
        }
    }

    @Test
    void weaponDrops_spawnFromSkyAndLandOnPlatform() {
        PlatformSurface surface = new PlatformSurface(100.0, 300.0, 260.0, 20.0, false);
        MatchDropCoordinator coordinator = new MatchDropCoordinator(List.of(surface), new FixedRandom());
        coordinator.prepareRound(0L);

        long now = GameSettings.FIRST_DROP_DELAY_MS + 1L;
        coordinator.updateDrops(now);

        assertEquals(1, coordinator.weaponDrops().size());
        WeaponDrop drop = coordinator.weaponDrops().get(0);
        assertTrue(drop.y() < 0.0);
        assertFalse(drop.isLanded());

        long simNow = now;
        for (int i = 0; i < 360 && !drop.isLanded(); i++) {
            simNow += 16L;
            coordinator.updateDrops(simNow);
        }

        assertTrue(drop.isLanded());
        assertEquals(surface.getBounds().getMinY() - GameSettings.BOX_SIZE - 2.0, drop.y(), 1e-6);
    }

    @Test
    void barrelTraps_spawnFromSkyAndLandOnPlatform() {
        PlatformSurface surface = new PlatformSurface(120.0, 280.0, 300.0, 20.0, false);
        MatchDropCoordinator coordinator = new MatchDropCoordinator(List.of(surface), new FixedRandom());
        coordinator.prepareRound(0L);

        Trap trap = coordinator.traps().stream()
                .filter(t -> t instanceof ExplosiveBarrel)
                .findFirst()
                .orElseThrow();

        double landingY = surface.getBounds().getMinY() - 42.0;
        assertTrue(trap.getBounds().getMinY() < landingY);

        for (int i = 0; i < 360; i++) {
            trap.update(1.0 / 60.0);
        }

        assertEquals(landingY, trap.getBounds().getMinY(), 1e-6);
    }
}
