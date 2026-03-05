package game.ui;

import game.config.GameSettings;
import game.entities.powerups.PowerUp;
import game.entities.powerups.ShieldPowerUp;
import game.entities.powerups.SpeedPowerUp;
import game.entities.traps.ExplosiveBarrel;
import game.entities.traps.Landmine;
import game.entities.traps.Trap;
import game.entities.weapons.Gun;
import game.entities.weapons.GunRegistry;
import game.map.PlatformSurface;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

final class MatchDropCoordinator {
    private final List<PlatformSurface> worldSurfaces;
    private final Random random;
    private final List<WeaponDrop> weaponDrops = new ArrayList<>();
    private final List<Trap> traps = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();

    private long nextGunDropAtMillis;
    private long nextTrapDropAtMillis;
    private long nextPowerUpDropAtMillis;

    MatchDropCoordinator(List<PlatformSurface> worldSurfaces, Random random) {
        this.worldSurfaces = worldSurfaces;
        this.random = random;
    }

    List<WeaponDrop> weaponDrops() {
        return weaponDrops;
    }

    List<Trap> traps() {
        return traps;
    }

    List<PowerUp> powerUps() {
        return powerUps;
    }

    long nextGunDropAtMillis() {
        return nextGunDropAtMillis;
    }

    void clearRoundItems() {
        weaponDrops.clear();
        traps.clear();
        powerUps.clear();
    }

    void prepareRound(long freezeUntilMillis) {
        clearRoundItems();
        spawnTraps();
        nextGunDropAtMillis = freezeUntilMillis + GameSettings.FIRST_DROP_DELAY_MS;
        nextTrapDropAtMillis = freezeUntilMillis + GameSettings.TRAP_DROP_INTERVAL_MS;
        nextPowerUpDropAtMillis = freezeUntilMillis + GameSettings.POWERUP_DROP_INTERVAL_MS;
    }

    void updateDrops(long nowMillis) {
        updateWeaponDrops(nowMillis);
        updateTrapDrops(nowMillis);
        updatePowerUpDrops(nowMillis);
    }

    void renderWeaponDrops(GraphicsContext gc) {
        for (WeaponDrop drop : weaponDrops) {
            gc.setFill(Color.web("#ffe69a"));
            gc.fillRoundRect(drop.x(), drop.y(), GameSettings.BOX_SIZE, GameSettings.BOX_SIZE, 8, 8);
            gc.setStroke(Color.web("#8f5f00"));
            gc.setLineWidth(2.0);
            gc.strokeRoundRect(drop.x(), drop.y(), GameSettings.BOX_SIZE, GameSettings.BOX_SIZE, 8, 8);
            gc.drawImage(drop.gun().sprite(), drop.x() + 4, drop.y() + 9, GameSettings.BOX_SIZE - 8, 14);
        }
    }

    private void spawnTraps() {
        if (worldSurfaces.isEmpty()) {
            return;
        }

        int numTraps = random.nextInt(3) + 1;
        List<PlatformSurface> candidates = worldSurfaces.stream()
                .filter(surface -> surface.getBounds().getWidth() > 70.0)
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        for (int i = 0; i < numTraps; i++) {
            PlatformSurface surface = candidates.get(random.nextInt(candidates.size()));
            var bounds = surface.getBounds();
            double spawnMinX = bounds.getMinX() + 6.0;
            double spawnMaxX = bounds.getMaxX() - GameSettings.BARREL_SIZE - 6.0;
            if (spawnMaxX > spawnMinX) {
                double spawnX = spawnMinX + random.nextDouble() * (spawnMaxX - spawnMinX);
                double spawnY = bounds.getMinY();
                if (random.nextBoolean()) {
                    traps.add(new ExplosiveBarrel(spawnX, spawnY));
                } else {
                    traps.add(new Landmine(spawnX, spawnY));
                }
            }
        }
    }

    private void updatePowerUpDrops(long nowMillis) {
        if (nowMillis < nextPowerUpDropAtMillis) {
            return;
        }
        if (powerUps.size() >= 2) {
            nextPowerUpDropAtMillis = nowMillis + GameSettings.POWERUP_DROP_INTERVAL_MS;
            return;
        }
        if (worldSurfaces.isEmpty()) {
            return;
        }

        List<PlatformSurface> candidates = worldSurfaces.stream()
                .filter(surface -> surface.getBounds().getWidth() > 40.0)
                .toList();
        if (!candidates.isEmpty()) {
            PlatformSurface surface = candidates.get(random.nextInt(candidates.size()));
            var bounds = surface.getBounds();
            double spawnMinX = bounds.getMinX() + 6.0;
            double spawnMaxX = bounds.getMaxX() - 26.0 - 6.0;
            if (spawnMaxX > spawnMinX) {
                double spawnX = spawnMinX + random.nextDouble() * (spawnMaxX - spawnMinX);
                double spawnY = bounds.getMinY() - 28.0;
                if (random.nextBoolean()) {
                    powerUps.add(new ShieldPowerUp(spawnX, spawnY));
                } else {
                    powerUps.add(new SpeedPowerUp(spawnX, spawnY));
                }
            }
        }
        nextPowerUpDropAtMillis = nowMillis + GameSettings.POWERUP_DROP_INTERVAL_MS;
    }

    private void updateTrapDrops(long nowMillis) {
        if (nowMillis < nextTrapDropAtMillis) {
            return;
        }
        if (traps.size() >= 3) {
            nextTrapDropAtMillis = nowMillis + GameSettings.TRAP_DROP_INTERVAL_MS;
            return;
        }
        if (worldSurfaces.isEmpty()) {
            return;
        }

        List<PlatformSurface> candidates = worldSurfaces.stream()
                .filter(surface -> surface.getBounds().getWidth() > 70.0)
                .toList();
        if (!candidates.isEmpty()) {
            PlatformSurface surface = candidates.get(random.nextInt(candidates.size()));
            var bounds = surface.getBounds();
            double spawnMinX = bounds.getMinX() + 6.0;
            double spawnMaxX = bounds.getMaxX() - GameSettings.BARREL_SIZE - 6.0;
            if (spawnMaxX > spawnMinX) {
                double spawnX = spawnMinX + random.nextDouble() * (spawnMaxX - spawnMinX);
                double spawnY = bounds.getMinY();
                if (random.nextBoolean()) {
                    traps.add(new ExplosiveBarrel(spawnX, spawnY));
                } else {
                    traps.add(new Landmine(spawnX, spawnY));
                }
            }
        }
        nextTrapDropAtMillis = nowMillis + GameSettings.TRAP_DROP_INTERVAL_MS;
    }

    private void updateWeaponDrops(long nowMillis) {
        if (nowMillis < nextGunDropAtMillis) {
            return;
        }
        if (weaponDrops.size() >= 2) {
            nextGunDropAtMillis = nowMillis + GameSettings.NEXT_DROP_INTERVAL_MS;
            return;
        }

        WeaponDrop drop = createWeaponDrop();
        if (drop != null) {
            weaponDrops.add(drop);
        }
        nextGunDropAtMillis = nowMillis + GameSettings.NEXT_DROP_INTERVAL_MS;
    }

    private WeaponDrop createWeaponDrop() {
        if (worldSurfaces.isEmpty()) {
            return null;
        }

        List<PlatformSurface> candidates = worldSurfaces.stream()
                .filter(surface -> surface.getBounds().getWidth() > GameSettings.BOX_SIZE + 20.0)
                .sorted(Comparator.comparingDouble(surface -> surface.getBounds().getMinY()))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }

        PlatformSurface selectedSurface = candidates.get(random.nextInt(candidates.size()));
        var bounds = selectedSurface.getBounds();
        double spawnMinX = bounds.getMinX() + 6.0;
        double spawnMaxX = bounds.getMaxX() - GameSettings.BOX_SIZE - 6.0;
        double spawnX = spawnMinX + random.nextDouble() * Math.max(1.0, spawnMaxX - spawnMinX);
        double spawnY = bounds.getMinY() - GameSettings.BOX_SIZE - 2.0;

        List<Gun> randomPool = GunRegistry.SELECTABLE_GUNS;
        Gun gun = randomPool.get(random.nextInt(randomPool.size()));
        return new WeaponDrop(spawnX, spawnY, gun);
    }
}
