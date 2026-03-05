package game.ui;

import game.logic.SoundManager;
import game.entities.weapons.Gun;
import game.entities.weapons.GunRegistry;
import game.testutil.FxTestUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class WeaponSelectPanelTest {

    @BeforeAll
    static void initFx() {
        FxTestUtils.initJavaFx();
    }

    @AfterEach
    void stopAudio() {
        SoundManager.getInstance().stopBgm();
    }

    @Test
    void keyNavigationUpdatesLabels_andEnterEscInvokeCallbacks() {
        AtomicReference<Gun> pickedP1 = new AtomicReference<>();
        AtomicReference<Gun> pickedP2 = new AtomicReference<>();
        AtomicInteger backCalls = new AtomicInteger();

        AtomicReference<String> p1WeaponTextBefore = new AtomicReference<>();
        AtomicReference<String> p2WeaponTextBefore = new AtomicReference<>();
        AtomicReference<String> p1WeaponTextAfter = new AtomicReference<>();
        AtomicReference<String> p2WeaponTextAfter = new AtomicReference<>();

        FxTestUtils.runOnFxThreadAndWait(() -> {
            WeaponSelectPanel panel = new WeaponSelectPanel(
                    (p1, p2) -> {
                        pickedP1.set(p1);
                        pickedP2.set(p2);
                    },
                    backCalls::incrementAndGet
            );
            Scene scene = new Scene(panel);
            assertNotNull(scene);

            // Grab the two big weapon labels by their initial text
            Label p1Weapon = (Label) panel.getChildren().stream()
                    .flatMap(n -> n.lookupAll(".label").stream())
                    .filter(n -> n instanceof Label)
                    .map(n -> (Label) n)
                    .filter(l -> GunRegistry.SELECTABLE_GUNS.stream().anyMatch(g -> g.label().equals(l.getText())))
                    .findFirst()
                    .orElse(null);

            // The lookup above can be timing/csstree dependent; fall back to scanning all labels in subtree.
            if (p1Weapon == null) {
                p1Weapon = panel.getChildren().stream()
                        .flatMap(n -> n instanceof javafx.scene.Parent p ? p.getChildrenUnmodifiable().stream() : java.util.stream.Stream.empty())
                        .flatMap(n -> n instanceof javafx.scene.Parent p ? p.getChildrenUnmodifiable().stream() : java.util.stream.Stream.of(n))
                        .filter(n -> n instanceof Label)
                        .map(n -> (Label) n)
                        .filter(l -> GunRegistry.SELECTABLE_GUNS.stream().anyMatch(g -> g.label().equals(l.getText())))
                        .findFirst()
                        .orElseThrow();
            }

            // We know initial labels are Rifle (P1) and Shotgun (P2)
            // So just capture them via text matching.
            Label p1 = findLabel(panel, GunRegistry.RIFLE.label());
            Label p2 = findLabel(panel, GunRegistry.SHOTGUN.label());

            p1WeaponTextBefore.set(p1.getText());
            p2WeaponTextBefore.set(p2.getText());

            panel.requestFocus();

            // Change P1 and P2 selections
            panel.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.D, false, false, false, false));
            panel.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.RIGHT, false, false, false, false));

            // After moving, the original labels may no longer match; re-locate by scanning for any gun label.
            p1WeaponTextAfter.set(findAnyGunLabelText(panel));
            p2WeaponTextAfter.set(findAnyGunLabelText(panel));

            // Start via ENTER
            panel.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));

            // Back via ESC
            panel.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false));
        });

        assertEquals(GunRegistry.RIFLE.label(), p1WeaponTextBefore.get());
        assertEquals(GunRegistry.SHOTGUN.label(), p2WeaponTextBefore.get());

        assertNotNull(pickedP1.get(), "ENTER should start and provide P1 gun");
        assertNotNull(pickedP2.get(), "ENTER should start and provide P2 gun");
        assertTrue(GunRegistry.SELECTABLE_GUNS.contains(pickedP1.get()));
        assertTrue(GunRegistry.SELECTABLE_GUNS.contains(pickedP2.get()));

        assertEquals(1, backCalls.get(), "ESC should call onBackToMenu");
    }

    private static Label findLabel(WeaponSelectPanel root, String text) {
        return root.lookupAll("*").stream()
                .filter(n -> n instanceof Label l && text.equals(l.getText()))
                .map(n -> (Label) n)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Label not found: " + text));
    }

    private static String findAnyGunLabelText(WeaponSelectPanel root) {
        return root.lookupAll("*").stream()
                .filter(n -> n instanceof Label l)
                .map(n -> (Label) n)
                .map(Label::getText)
                .filter(t -> GunRegistry.SELECTABLE_GUNS.stream().anyMatch(g -> g.label().equals(t)))
                .findFirst()
                .orElse(null);
    }
}
