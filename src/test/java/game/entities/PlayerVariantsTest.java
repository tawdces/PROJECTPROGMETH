package game.entities;

import game.logic.SpriteFrame;
import game.testutil.FxTestUtils;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerVariantsTest {

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @Test
    void player_constructorCanCreateP1StyleInstance() {
        Player p1a = new Player(10, 20, "P1", Color.DODGERBLUE, 1, null, List.of());
        assertNotNull(p1a);
        assertEquals(1, p1a.getFacingDirection());

        Player p1b = new Player(10, 20, "P1", Color.DODGERBLUE, 1, null, List.of(new SpriteFrame(0, 0, 1, 1)));
        assertNotNull(p1b);
        assertEquals(1, p1b.getFacingDirection());
    }

    @Test
    void player_constructorCanCreateP2StyleInstance() {
        Player p2a = new Player(30, 40, "P2", Color.CRIMSON, -1, null, List.of());
        assertNotNull(p2a);
        assertEquals(-1, p2a.getFacingDirection());

        Player p2b = new Player(30, 40, "P2", Color.CRIMSON, -1, null, List.of(new SpriteFrame(0, 0, 1, 1)));
        assertNotNull(p2b);
        assertEquals(-1, p2b.getFacingDirection());
    }
}
