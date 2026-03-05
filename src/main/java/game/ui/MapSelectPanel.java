package game.ui;

import game.config.GameSettings;
import game.logic.SoundManager;
import game.map.GameMap;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MapSelectPanel extends VBox {

    public static int mapWidth = 540;
    public static int mapHeight = 300;
    public static final String SUNSET_MAP_RESOURCE = GameMap.SUNSET_RESOURCE;
    private static final List<GameMap> MAPS = GameMap.availableMaps();
    public static final List<String> MAP_RESOURCES = MAPS.stream()
            .map(GameMap::resourcePath)
            .collect(Collectors.toUnmodifiableList());

    public MapSelectPanel(Consumer<GameMap> onMapSelected, Runnable onBackToMenu) {
        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        setAlignment(Pos.CENTER);

        setSpacing(10);
        setStyle(
                "-fx-background-color: linear-gradient(to bottom, #11192b 0%, #1f3048 50%, #102638 100%); "
                        + "-fx-padding: 20;"
        );

        Label title = new Label("SELECT ARENA");
        title.setTextFill(Color.web("#fff4af"));
        title.setFont(Font.font("Impact", FontWeight.NORMAL, 45));

        int[] index = {0};
        ImageView mapPreview = new ImageView(loadMap(index[0]));

        mapPreview.setFitWidth(mapWidth*0.8);
        mapPreview.setFitHeight(mapHeight*0.8);
        mapPreview.setPreserveRatio(false);

        StackPane previewFrame = new StackPane(mapPreview);
        previewFrame.setStyle(
                "-fx-background-color: rgba(0,0,0,0.36); "
                        + "-fx-border-color: rgba(255,225,117,0.88); "
                        + "-fx-border-width: 2; "
                        + "-fx-padding: 8; "
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


        Button back = new Button("BACK");
        back.setPrefWidth(220);
        styleButton(back, "#5a6577", "#3a4354");
        back.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            onBackToMenu.run();
        });

        Button next = new Button("NEXT");
        next.setPrefWidth(220);
        styleButton(next, "#3c8cff", "#1f5ec9");
        next.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            onMapSelected.accept(MAPS.get(index[0]));
        });


        HBox buttonLayout = new HBox(20);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(back, next);


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
                onMapSelected.accept(MAPS.get(index[0]));
            } else if (event.getCode() == KeyCode.ESCAPE) {
                SoundManager.getInstance().playEffect("click");
                onBackToMenu.run();
            }
        });


        getChildren().addAll(title, previewFrame, mapName, page, hint, buttonLayout);
    }

    private static Image loadMap(int index) {
        String previewResource = MAPS.get(index).previewResourcePath();

        var url = MapSelectPanel.class.getResource(previewResource);
        if (url != null) {
            return new Image(url.toExternalForm());
        }

        Path fallback = Paths.get("src", "main", "resources", previewResource.replaceFirst("^/", ""));
        if (Files.exists(fallback)) {
            return new Image(fallback.toUri().toString());
        }
        throw new IllegalArgumentException("Map resource not found: " + previewResource);
    }

    private static String labelFor(int index) {
        return MAPS.get(index).label();
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
