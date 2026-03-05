package game.entities;

import game.config.GameSettings;
import game.map.PlatformSurface;
import game.logic.SpriteFrame;
import game.entities.weapons.GunRegistry;
import game.testutil.FxTestUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    static final class TestPlayer extends Player {
        TestPlayer(double x, double y, int facing) {
            super(x, y, "T", Color.BLACK, facing, null, List.of(new SpriteFrame(0, 0, 1, 1)));
        }
    }

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @Test
    void setHorizontalInput_setsFacingDirection() {
        Player p = new TestPlayer(0, 0, 1);

        p.setHorizontalInput(-1.0);
        assertEquals(-1, p.getFacingDirection());

        p.setHorizontalInput(1.0);
        assertEquals(1, p.getFacingDirection());

        int facingBefore = p.getFacingDirection();
        p.setHorizontalInput(0.0);
        assertEquals(facingBefore, p.getFacingDirection());
    }

    @Test
    void jump_changesVerticalMotion() {
        Player p = new TestPlayer(0, 100, 1);

        double y0 = p.getBounds().getMinY();
        p.jump(System.currentTimeMillis());
        p.update(0.05);

        assertTrue(p.getBounds().getMinY() < y0, "Jump should move player up initially (negative Y velocity)");
    }

    @Test
    void update_movesByInputAndGravity() {
        Player p = new TestPlayer(0, 0, 1);

        p.setHorizontalInput(1.0);
        double x0 = p.getBounds().getMinX();
        double y0 = p.getBounds().getMinY();

        p.update(0.1);

        assertTrue(p.getBounds().getMinX() > x0);
        assertTrue(p.getBounds().getMinY() >= y0, "Without jump, gravity should pull down or keep same initially");
    }

    @Test
    void isMeleeHit_dependsOnFacingDirectionAndRange() {
        Player attackerRight = new TestPlayer(100, 100, 1);
        Player target = new TestPlayer(100 + GameSettings.PLAYER_WIDTH + 5, 100, -1);

        assertTrue(attackerRight.isMeleeHit(target));

        Player attackerLeft = new TestPlayer(200, 100, -1);
        Player targetLeft = new TestPlayer(200 - GameSettings.MELEE_RANGE - 5, 100, 1);
        assertTrue(attackerLeft.isMeleeHit(targetLeft));

        Player far = new TestPlayer(1000, 1000, 1);
        assertFalse(attackerRight.isMeleeHit(far));
    }

    @Test
    void applyKnockback_affectsNextUpdate() {
        Player p = new TestPlayer(0, 0, 1);

        double x0 = p.getBounds().getMinX();
        p.applyKnockback(500.0, 0.0);
        p.update(0.1);

        assertTrue(p.getBounds().getMinX() > x0, "Knockback X should push player horizontally");
    }

    @Test
    void resolveCollisions_landsOnSurfaceAndSetsOnGround() {
        Player p = new TestPlayer(100, 0, 1);

        double surfaceY = 200;
        PlatformSurface surface = new PlatformSurface(0, surfaceY, 1000, 20, false);

        for (int i = 0; i < 60; i++) {
            p.update(1.0 / 60.0);
            p.resolveCollisions(List.of(surface), System.currentTimeMillis());
            if (p.isOnGround()) {
                break;
            }
        }

        assertTrue(p.isOnGround(), "Player should eventually land on the platform");
        assertEquals(surfaceY - GameSettings.PLAYER_HEIGHT, p.getBounds().getMinY(), 2.0);
    }

    @Test
    void requestDropThrough_onlyWorksWhenOnGround() {
        Player p = new TestPlayer(100, 0, 1);

        double surfaceY = 200;
        PlatformSurface oneWay = new PlatformSurface(0, surfaceY, 1000, 20, true);

        for (int i = 0; i < 120; i++) {
            p.update(1.0 / 60.0);
            p.resolveCollisions(List.of(oneWay), System.currentTimeMillis());
            if (p.isOnGround()) break;
        }
        assertTrue(p.isOnGround());

        double y0 = p.getBounds().getMinY();
        p.requestDropThrough(System.currentTimeMillis());

        assertFalse(p.isOnGround());
        assertTrue(p.getBounds().getMinY() > y0, "Drop-through nudges player downward");
    }

    @Test
    void clampX_clampsToRange() {
        Player p = new TestPlayer(0, 0, 1);

        // Force x beyond max by applying knockback and updating
        p.applyKnockback(10_000, 0);
        p.update(0.1);

        p.clampX(10, 50);
        assertTrue(p.getBounds().getMinX() >= 10 - 1e-6);
        assertTrue(p.getBounds().getMinX() <= 50 + 1e-6);

        // Clamp below min
        p.applyKnockback(-10_000, 0);
        p.update(0.1);
        p.clampX(10, 50);
        assertTrue(p.getBounds().getMinX() >= 10 - 1e-6);
    }

    @Test
    void shoot_unarmedReturnsEmpty_armedReturnsBullets() {
        Player p = new TestPlayer(100, 100, 1);

        assertTrue(p.shoot().isEmpty());

        p.equipPermanentGun(GunRegistry.PISTOL);
        List<Bullet> bullets = p.shoot();

        assertEquals(1, bullets.size());
        assertSame(p, bullets.get(0).getOwner());
    }

    @Test
    void hasGun_getEquippedGun_equipGun_expiryBehavior() {
        Player p = new TestPlayer(0, 0, 1);

        long t0 = 1_000_000L;
        assertFalse(p.hasGun(t0));
        assertSame(GunRegistry.UNARMED, p.getEquippedGun(t0));

        p.equipGun(GunRegistry.PISTOL, t0);
        assertTrue(p.hasGun(t0));
        assertSame(GunRegistry.PISTOL, p.getEquippedGun(t0));

        long afterExpiry = t0 + GunRegistry.PISTOL.durationMillis() + 1;
        assertSame(GunRegistry.UNARMED, p.getEquippedGun(afterExpiry), "After expiry, should fall back to fallback gun (default UNARMED)");
        assertFalse(p.hasGun(afterExpiry));
    }

    @Test
    void equipPermanentGun_makesGunNeverExpire() {
        Player p = new TestPlayer(0, 0, 1);

        p.equipPermanentGun(GunRegistry.RIFLE);

        long farFuture = Long.MAX_VALUE - 10_000;
        assertTrue(p.hasGun(farFuture));
        assertSame(GunRegistry.RIFLE, p.getEquippedGun(farFuture));
    }

    @Test
    void getShootCooldownMillis_unarmedVsArmed() {
        Player p = new TestPlayer(0, 0, 1);

        long now = 123L;
        assertEquals(GameSettings.SHOOT_COOLDOWN_MS, p.getShootCooldownMillis(now));

        p.equipPermanentGun(GunRegistry.MACHINE_GUN);
        assertEquals(GunRegistry.MACHINE_GUN.cooldownMillis(), p.getShootCooldownMillis(now));
    }

    @Test
    void canAction_setActionCooldown() {
        Player p = new TestPlayer(0, 0, 1);

        long now = 1000L;
        assertTrue(p.canAction(now));

        p.setActionCooldown(now, 500L);
        assertFalse(p.canAction(now));
        assertTrue(p.canAction(now + 500L));
    }

    @Test
    void isInvulnerable_and_respawnFromSky() {
        Player p = new TestPlayer(0, 0, 1);

        long now = 10_000L;
        p.respawnFromSky(300, 50, now);

        assertTrue(p.isInvulnerable(now));
        assertFalse(p.canAction(now), "respawnFromSky sets a short action cooldown");
        assertEquals(300, p.getBounds().getMinX(), 1e-6);
        assertEquals(50, p.getBounds().getMinY(), 1e-6);

        long afterInvuln = now + GameSettings.RESPAWN_INVULNERABILITY_MS + 1;
        assertFalse(p.isInvulnerable(afterInvuln));
    }

    @Test
    void render_withSpeedBoostTrail_doesNotThrow() {
        Player p = new TestPlayer(0, 0, 1);
        Canvas canvas = new Canvas(220, 220);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        p.applySpeedBoost(1.6, 5_000L, System.currentTimeMillis());
        p.setHorizontalInput(1.0);
        p.update(0.05);
        p.update(0.05);

        assertDoesNotThrow(() -> p.render(gc));
    }
}
