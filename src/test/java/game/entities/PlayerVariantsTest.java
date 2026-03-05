package game.entities;

import game.logic.SpriteFrame;
import game.testutil.FxTestUtils;
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
    void playerOne_constructorsCreateInstance() {
        PlayerOne p1a = new PlayerOne(10, 20);
        assertNotNull(p1a);

        PlayerOne p1b = new PlayerOne(10, 20, null, List.of(new SpriteFrame(0, 0, 1, 1)));
        assertNotNull(p1b);
    }

    @Test
    void playerTwo_constructorsCreateInstance() {
        PlayerTwo p2a = new PlayerTwo(30, 40);
        assertNotNull(p2a);

        PlayerTwo p2b = new PlayerTwo(30, 40, null, List.of(new SpriteFrame(0, 0, 1, 1)));
        assertNotNull(p2b);
    }
}
