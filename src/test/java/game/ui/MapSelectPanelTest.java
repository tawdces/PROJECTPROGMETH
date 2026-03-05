package game.ui;

import game.logic.SoundManager;
import game.map.GameMap;
import game.testutil.FxTestUtils;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MapSelectPanelTest {

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @AfterEach
    void stopAudio() {
        SoundManager.getInstance().stopBgm();
    }

    @Test
    void constructs_andKeyEventsTriggerCallbacks() {
        AtomicReference<GameMap> selectedMap = new AtomicReference<>();
        AtomicInteger backCalls = new AtomicInteger();

        FxTestUtils.runOnFxThreadAndWait(() -> {
            MapSelectPanel panel = new MapSelectPanel(selectedMap::set, backCalls::incrementAndGet);
            Scene scene = new Scene(panel);
            assertNotNull(scene);

            panel.requestFocus();

            // Change map right, then confirm selection via ENTER
            panel.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.RIGHT, false, false, false, false));
            panel.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));

            // Go back via ESC
            panel.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false));
        });

        assertNotNull(selectedMap.get(), "ENTER should select a map");
        assertTrue(MapSelectPanel.MAP_RESOURCES.contains(selectedMap.get().resourcePath()));
        assertEquals(1, backCalls.get(), "ESC should call onBackToMenu");
    }
}
