package game.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SpriteFrameTest {

    @Test
    void accessors_returnConstructorValues() {
        SpriteFrame frame = new SpriteFrame(10.0, 20.0, 30.0, 40.0);

        assertEquals(10.0, frame.x(), 0.000_001);
        assertEquals(20.0, frame.y(), 0.000_001);
        assertEquals(30.0, frame.width(), 0.000_001);
        assertEquals(40.0, frame.height(), 0.000_001);
    }

    @Test
    void valueEquality_usesAllRecordFields() {
        SpriteFrame a = new SpriteFrame(1.0, 2.0, 3.0, 4.0);
        SpriteFrame b = new SpriteFrame(1.0, 2.0, 3.0, 4.0);
        SpriteFrame c = new SpriteFrame(1.0, 2.0, 3.0, 5.0);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
