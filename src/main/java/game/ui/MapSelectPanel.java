package game.ui;

import game.config.GameSettings;
import game.core.SoundManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Consumer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MapSelectPanel extends VBox {

    public static final List<String> MAP_RESOURCES = List.of("/Map1.png", "/Map2.png", "/Map3.png");

    public MapSelectPanel(Consumer<String> onMapSelected, Runnable onBackToMenu) {
        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        setAlignment(Pos.CENTER);
        setSpacing(14);
        setStyle(
                "-fx-background-color: linear-gradient(to bottom, #11192b 0%, #1f3048 50%, #102638 100%); "
                        + "-fx-padding: 20;"
        );

        Label title = new Label("SELECT ARENA");
        title.setTextFill(Color.web("#fff4af"));
        title.setFont(Font.font("Impact", FontWeight.NORMAL, 50));

        int[] index = {0};
        ImageView mapPreview = new ImageView(loadMap(index[0]));
        mapPreview.setFitWidth(620);
        mapPreview.setFitHeight(360);
        mapPreview.setPreserveRatio(false);

        StackPane previewFrame = new StackPane(mapPreview);
        previewFrame.setStyle(
                "-fx-background-color: rgba(0,0,0,0.36); "
                        + "-fx-border-color: rgba(255,225,117,0.88); "
                        + "-fx-border-width: 2; "
                        + "-fx-padding: 10; "
                        + "-fx-background-radius: 10; "
                        + "-fx-border-radius: 10;"
        );

        Label mapName = new Label(labelFor(index[0]));
        mapName.setTextFill(Color.web("#d3e7ff"));
        mapName.setFont(Font.font("Consolas", FontWeight.BOLD, 22));

        Label page = new Label("Arena " + (index[0] + 1) + " / " + MAP_RESOURCES.size());
        page.setTextFill(Color.web("#ffe8a0"));
        page.setFont(Font.font("Consolas", FontWeight.BOLD, 16));

        Label hint = new Label("A/D or Left/Right to change map | Enter to continue | Esc to menu");
        hint.setTextFill(Color.web("#d3e7ff"));
        hint.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));

        Button next = new Button("NEXT: WEAPON SELECT");
        next.setPrefWidth(260);
        styleButton(next, "#3c8cff", "#1f5ec9");
        next.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            onMapSelected.accept(MAP_RESOURCES.get(index[0]));
        });

        Button back = new Button("BACK");
        back.setPrefWidth(260);
        styleButton(back, "#5a6577", "#3a4354");
        back.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            onBackToMenu.run();
        });

        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
                SoundManager.getInstance().playEffect("click");
                index[0] = cycleIndex(index[0], -1, MAP_RESOURCES.size());
                mapPreview.setImage(loadMap(index[0]));
                mapName.setText(labelFor(index[0]));
                page.setText("Arena " + (index[0] + 1) + " / " + MAP_RESOURCES.size());
            } else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
                SoundManager.getInstance().playEffect("click");
                index[0] = cycleIndex(index[0], 1, MAP_RESOURCES.size());
                mapPreview.setImage(loadMap(index[0]));
                mapName.setText(labelFor(index[0]));
                page.setText("Arena " + (index[0] + 1) + " / " + MAP_RESOURCES.size());
            } else if (event.getCode() == KeyCode.ENTER) {
                SoundManager.getInstance().playEffect("click");
                onMapSelected.accept(MAP_RESOURCES.get(index[0]));
            } else if (event.getCode() == KeyCode.ESCAPE) {
                SoundManager.getInstance().playEffect("click");
                onBackToMenu.run();
            }
        });

        getChildren().addAll(title, previewFrame, mapName, page, hint, next, back);
    }

    private static Image loadMap(int index) {
        String resource = MAP_RESOURCES.get(index);
        var url = MapSelectPanel.class.getResource(resource);
        if (url != null) {
            return new Image(url.toExternalForm());
        }

        Path fallback = Paths.get("src", "main", "resources", resource.replaceFirst("^/", ""));
        if (Files.exists(fallback)) {
            return new Image(fallback.toUri().toString());
        }
        throw new IllegalArgumentException("Map resource not found: " + resource);
    }

    private static String labelFor(int index) {
        return "Map " + (index + 1);
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
