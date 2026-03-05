package game.core;

import game.testutil.FxTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundManagerTest {

    @BeforeAll
    static void initFx() {
        // AudioClip/MediaPlayer live in JavaFX media; initialize the JavaFX platform first.
        FxTestUtils.initJavaFx();
    }

    @Test
    void getInstance_returnsSingleton() {
        SoundManager a = SoundManager.getInstance();
        SoundManager b = SoundManager.getInstance();
        assertSame(a, b);
    }

    @Test
    void playEffect_unknownName_isNoOpAndDoesNotThrow() {
        SoundManager sm = SoundManager.getInstance();
        assertDoesNotThrow(() -> sm.playEffect("this-effect-does-not-exist"));
    }

    @Test
    void stopBgm_isIdempotentAndDoesNotThrow() {
        SoundManager sm = SoundManager.getInstance();

        assertDoesNotThrow(sm::stopBgm);
        assertDoesNotThrow(sm::stopBgm);
    }

    @Test
    void playRandomBgm_doesNotThrow_evenIfMediaMissingOrUnsupported() {
        SoundManager sm = SoundManager.getInstance();

        // Implementation catches load/play failures internally; this test asserts it stays safe to call.
        assertDoesNotThrow(sm::playRandomBgm);

        // Always safe to stop afterwards (whether it started or not).
        assertDoesNotThrow(sm::stopBgm);
    }

    @Test
    void playMenuBgm_doesNotThrow_evenIfMediaMissingOrUnsupported() {
        SoundManager sm = SoundManager.getInstance();

        // Implementation catches load/play failures internally; this test asserts it stays safe to call.
        assertDoesNotThrow(sm::playMenuBgm);

        // Always safe to stop afterwards (whether it started or not).
        assertDoesNotThrow(sm::stopBgm);
    }
}
