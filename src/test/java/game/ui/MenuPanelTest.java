package game.ui;

import game.core.SoundManager;
import game.testutil.FxTestUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MenuPanelTest {

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @AfterEach
    void stopAudio() {
        SoundManager.getInstance().stopBgm();
    }

    @Test
    void constructs_andButtonsInvokeCallbacks() {
        AtomicInteger startCalls = new AtomicInteger();
        AtomicInteger exitCalls = new AtomicInteger();

        FxTestUtils.runOnFxThreadAndWait(() -> {
            MenuPanel panel = new MenuPanel(startCalls::incrementAndGet, exitCalls::incrementAndGet);
            Scene scene = new Scene(panel); // ensures proper initialization paths
            assertNotNull(scene);

            Button start = (Button) panel.getChildren().stream()
                    .filter(n -> n instanceof Button b && "START".equals(b.getText()))
                    .findFirst()
                    .orElseThrow();

            Button exit = (Button) panel.getChildren().stream()
                    .filter(n -> n instanceof Button b && "EXIT".equals(b.getText()))
                    .findFirst()
                    .orElseThrow();

            start.fire();
            exit.fire();
        });

        assertEquals(1, startCalls.get());
        assertEquals(1, exitCalls.get());
    }
}
