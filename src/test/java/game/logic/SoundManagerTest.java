package game.logic;

import game.testutil.FxTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundManagerTest {

    @BeforeAll
    static void initFx() {
        // AudioClip/MediaPlayer live in JavaFX media; initialize the JavaFX platform first.
        FxTestUtils.initJavaFx();
    }

    @AfterEach
    void resetAudioState() {
        SoundManager sm = SoundManager.getInstance();
        sm.stopBgm();
        sm.setMusicVolume(1.0);
        sm.setEffectVolume(1.0);
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
    void setMusicVolume_clampsToValidRange() {
        SoundManager sm = SoundManager.getInstance();

        sm.setMusicVolume(0.42);
        assertEquals(0.42, sm.getMusicVolume(), 0.000_001);

        sm.setMusicVolume(-10.0);
        assertEquals(0.0, sm.getMusicVolume(), 0.000_001);

        sm.setMusicVolume(10.0);
        assertEquals(1.0, sm.getMusicVolume(), 0.000_001);
    }

    @Test
    void setEffectVolume_clampsToValidRange() {
        SoundManager sm = SoundManager.getInstance();

        sm.setEffectVolume(0.66);
        assertEquals(0.66, sm.getEffectVolume(), 0.000_001);

        sm.setEffectVolume(-10.0);
        assertEquals(0.0, sm.getEffectVolume(), 0.000_001);

        sm.setEffectVolume(10.0);
        assertEquals(1.0, sm.getEffectVolume(), 0.000_001);
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
