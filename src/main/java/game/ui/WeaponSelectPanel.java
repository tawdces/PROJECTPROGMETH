package game.ui;

import game.config.GameSettings;
import game.entities.weapons.Gun;
import game.entities.weapons.GunRegistry;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.BiConsumer;

public class WeaponSelectPanel extends VBox {

    public WeaponSelectPanel(BiConsumer<Gun, Gun> onStart, Runnable onBackToMenu) {
        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        setAlignment(Pos.CENTER);
        setSpacing(14);
        setStyle(
                "-fx-background-color: linear-gradient(to bottom, #11192b 0%, #1e2f47 55%, #11293c 100%); "
                        + "-fx-padding: 20;"
        );

        Label title = new Label("LOADOUT SELECT");
        title.setTextFill(Color.web("#fff4af"));
        title.setFont(Font.font("Impact", FontWeight.NORMAL, 50));

        List<Gun> choices = GunRegistry.SELECTABLE_GUNS;

        Label p1Label = new Label("Player 1  (A / D)");
        p1Label.setTextFill(Color.web("#d6ebff"));
        p1Label.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        Label p1Weapon = new Label(GunRegistry.RIFLE.label());
        p1Weapon.setTextFill(Color.web("#7fd1ff"));
        p1Weapon.setFont(Font.font("Impact", FontWeight.NORMAL, 34));

        Label p2Label = new Label("Player 2  (Left / Right)");
        p2Label.setTextFill(Color.web("#ffd6dc"));
        p2Label.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        Label p2Weapon = new Label(GunRegistry.SHOTGUN.label());
        p2Weapon.setTextFill(Color.web("#ff9f8d"));
        p2Weapon.setFont(Font.font("Impact", FontWeight.NORMAL, 34));

        int[] p1Index = {choices.indexOf(GunRegistry.RIFLE)};
        int[] p2Index = {choices.indexOf(GunRegistry.SHOTGUN)};

        VBox left = new VBox(12, p1Label, p1Weapon);
        left.setAlignment(Pos.CENTER);
        left.setPrefWidth(360);
        left.setStyle(
                "-fx-background-color: rgba(0,0,0,0.30); "
                        + "-fx-border-color: rgba(127,209,255,0.95); "
                        + "-fx-border-width: 2; "
                        + "-fx-padding: 20; "
                        + "-fx-background-radius: 10; "
                        + "-fx-border-radius: 10;"
        );

        VBox right = new VBox(12, p2Label, p2Weapon);
        right.setAlignment(Pos.CENTER);
        right.setPrefWidth(360);
        right.setStyle(
                "-fx-background-color: rgba(0,0,0,0.30); "
                        + "-fx-border-color: rgba(255,154,170,0.95); "
                        + "-fx-border-width: 2; "
                        + "-fx-padding: 20; "
                        + "-fx-background-radius: 10; "
                        + "-fx-border-radius: 10;"
        );

        HBox selectors = new HBox(28, left, right);
        selectors.setAlignment(Pos.CENTER);

        Button start = new Button("START");
        start.setPrefWidth(230);
        styleButton(start, "#3c8cff", "#1f5ec9");
        start.setOnAction(event -> onStart.accept(choices.get(p1Index[0]), choices.get(p2Index[0])));

        Button back = new Button("BACK");
        back.setPrefWidth(230);
        styleButton(back, "#5a6577", "#3a4354");
        back.setOnAction(event -> onBackToMenu.run());

        Label hint = new Label("A/D change P1 | Left/Right change P2 | Enter to start | Esc to menu");
        hint.setTextFill(Color.web("#d3e7ff"));
        hint.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));

        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.A) {
                p1Index[0] = cycleIndex(p1Index[0], -1, choices.size());
                p1Weapon.setText(choices.get(p1Index[0]).label());
            } else if (event.getCode() == KeyCode.D) {
                p1Index[0] = cycleIndex(p1Index[0], 1, choices.size());
                p1Weapon.setText(choices.get(p1Index[0]).label());
            } else if (event.getCode() == KeyCode.LEFT) {
                p2Index[0] = cycleIndex(p2Index[0], -1, choices.size());
                p2Weapon.setText(choices.get(p2Index[0]).label());
            } else if (event.getCode() == KeyCode.RIGHT) {
                p2Index[0] = cycleIndex(p2Index[0], 1, choices.size());
                p2Weapon.setText(choices.get(p2Index[0]).label());
            } else if (event.getCode() == KeyCode.ENTER) {
                onStart.accept(choices.get(p1Index[0]), choices.get(p2Index[0]));
            } else if (event.getCode() == KeyCode.ESCAPE) {
                onBackToMenu.run();
            }
        });

        getChildren().addAll(title, selectors, hint, start, back);
    }

    private static int cycleIndex(int current, int direction, int size) {
        return (current + direction + size) % size;
    }

    private static void styleButton(Button button, String top, String bottom) {
        button.setFocusTraversable(false);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Impact", FontWeight.NORMAL, 24));
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + top + ", " + bottom + "); "
                        + "-fx-background-radius: 10; "
                        + "-fx-padding: 8 16 8 16;"
        );
    }
}
