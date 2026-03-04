package game.entities;

import game.core.SpriteFrame;
import game.entities.weapons.*;
import game.testutil.FxTestUtils;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeaponsTest {

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
    void unarmedGun_methodsAndFire() {
        Gun g = new UnarmedGun();

        assertEquals("OFF", g.label());
        assertEquals(0L, g.durationMillis());
        assertEquals(0L, g.cooldownMillis());
        assertNotNull(g.sprite());
        assertEquals(0.0, g.renderWidth(), 1e-9);

        Player owner = new TestPlayer(0, 0, 1);
        assertTrue(g.fire(owner, 10, 10, 1).isEmpty());
    }

    @Test
    void gunRegistry_constantsArePresent() {
        assertNotNull(GunRegistry.UNARMED);
        assertNotNull(GunRegistry.PISTOL);
        assertNotNull(GunRegistry.RIFLE);
        assertNotNull(GunRegistry.MACHINE_GUN);
        assertNotNull(GunRegistry.SHOTGUN);

        assertEquals(4, GunRegistry.SELECTABLE_GUNS.size());
        assertTrue(GunRegistry.SELECTABLE_GUNS.contains(GunRegistry.PISTOL));
        assertTrue(GunRegistry.SELECTABLE_GUNS.contains(GunRegistry.RIFLE));
        assertTrue(GunRegistry.SELECTABLE_GUNS.contains(GunRegistry.MACHINE_GUN));
        assertTrue(GunRegistry.SELECTABLE_GUNS.contains(GunRegistry.SHOTGUN));
    }

    @Test
    void pistolGun_inheritedMethodsAndFire() {
        Gun g = new PistolGun();

        assertEquals("Pistol", g.label());
        assertTrue(g.durationMillis() > 0);
        assertTrue(g.cooldownMillis() > 0);
        assertNotNull(g.sprite());
        assertTrue(g.renderWidth() > 0);

        Player owner = new TestPlayer(0, 0, 1);
        List<Bullet> bullets = g.fire(owner, 100, 50, 1);

        assertEquals(1, bullets.size());
        assertSame(owner, bullets.get(0).getOwner());
        assertTrue(bullets.get(0).getImpactForceX() > 0, "Facing right should produce +impactForceX");
    }

    @Test
    void rifleGun_inheritedMethodsAndFire() {
        Gun g = new RifleGun();

        assertEquals("Rifle", g.label());
        assertTrue(g.durationMillis() > 0);
        assertTrue(g.cooldownMillis() > 0);
        assertNotNull(g.sprite());
        assertTrue(g.renderWidth() > 0);

        Player owner = new TestPlayer(0, 0, -1);
        List<Bullet> bullets = g.fire(owner, 100, 50, -1);

        assertEquals(1, bullets.size());
        assertSame(owner, bullets.get(0).getOwner());
        assertTrue(bullets.get(0).getImpactForceX() < 0, "Facing left should produce -impactForceX");
    }

    @Test
    void machineGun_inheritedMethodsAndFire() {
        Gun g = new MachineGun();

        assertEquals("Machine", g.label());
        assertTrue(g.durationMillis() > 0);
        assertTrue(g.cooldownMillis() > 0);
        assertNotNull(g.sprite());
        assertTrue(g.renderWidth() > 0);

        Player owner = new TestPlayer(0, 0, 1);
        List<Bullet> bullets = g.fire(owner, 100, 50, 1);

        assertEquals(1, bullets.size());
    }

    @Test
    void shotgunGun_overriddenFireCreatesSpread() {
        Gun g = new ShotgunGun();

        assertEquals("Shotgun", g.label());
        assertTrue(g.durationMillis() > 0);
        assertTrue(g.cooldownMillis() > 0);
        assertNotNull(g.sprite());
        assertTrue(g.renderWidth() > 0);

        Player owner = new TestPlayer(0, 0, 1);
        List<Bullet> bullets = g.fire(owner, 100, 50, 1);

        assertEquals(5, bullets.size(), "Shotgun should fire multiple pellets");
        assertTrue(bullets.stream().allMatch(b -> b.getOwner() == owner));
    }
}
