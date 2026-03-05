package game.ui;

import game.entities.weapons.GunRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeaponDropTest {

    @Test
    void landedConstructor_startsLandedAtGivenY() {
        WeaponDrop drop = new WeaponDrop(20.0, 120.0, GunRegistry.PISTOL);

        assertTrue(drop.isLanded());
        assertEquals(20.0, drop.x(), 1e-9);
        assertEquals(120.0, drop.y(), 1e-9);
    }

    @Test
    void fallingConstructor_landsAfterUpdates() {
        WeaponDrop drop = new WeaponDrop(30.0, -90.0, 150.0, GunRegistry.RIFLE);
        assertFalse(drop.isLanded());
        assertTrue(drop.y() < 0.0);

        for (int i = 0; i < 300; i++) {
            drop.update(1.0 / 60.0);
        }

        assertTrue(drop.isLanded());
        assertEquals(150.0, drop.y(), 1e-6);
    }
}
