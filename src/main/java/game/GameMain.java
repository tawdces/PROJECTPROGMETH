package game;

import game.config.GameSettings;
import game.entities.weapons.Gun;
import game.ui.GamePanel;
import game.ui.MapSelectPanel;
import game.ui.MenuPanel;
import game.ui.WeaponSelectPanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GameMain extends Application {

    private Stage stage;
    private Scene appScene;
    private final Group scaledContent = new Group();
    private final StackPane responsiveRoot = new StackPane(scaledContent);
    private String selectedMapResource = "/Map1.png";

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Gun Mayhem Arena");
        stage.setResizable(true);
        stage.setMinWidth(640);
        stage.setMinHeight(420);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.maximizedProperty().addListener((obs, wasMaximized, isMaximized) -> {
            if (isMaximized && !stage.isFullScreen()) {
                stage.setMaximized(false);
                stage.setFullScreen(true);
            }
        });
        setupResponsiveRoot();
        appScene = new Scene(responsiveRoot, GameSettings.WIDTH, GameSettings.HEIGHT);
        installWindowShortcuts(appScene);
        stage.setScene(appScene);
        showMenu();
        stage.show();
    }

    public void showMenu() {
        MenuPanel menu = new MenuPanel(this::showMapSelect, stage::close);
        setContent(menu);
    }

    public void showMapSelect() {
        MapSelectPanel mapSelect = new MapSelectPanel(this::showWeaponSelect, this::showMenu);
        setContent(mapSelect);
        Platform.runLater(mapSelect::requestFocus);
    }

    public void showWeaponSelect(String mapResource) {
        selectedMapResource = mapResource;
        WeaponSelectPanel weaponSelect = new WeaponSelectPanel(this::startGame, this::showMapSelect);
        setContent(weaponSelect);
        Platform.runLater(weaponSelect::requestFocus);
    }

    public void startGame(Gun p1Weapon, Gun p2Weapon) {
        GamePanel gamePanel = new GamePanel(
                selectedMapResource,
                p1Weapon,
                p2Weapon,
                () -> showWeaponSelect(selectedMapResource),
                this::showMenu
        );
        setContent(gamePanel);
        gamePanel.bindInput(appScene);
        Platform.runLater(gamePanel::requestFocus);
    }

    private void setupResponsiveRoot() {
        responsiveRoot.setStyle("-fx-background-color: black;");

        var scaleBinding = Bindings.createDoubleBinding(
                () -> {
                    double width = responsiveRoot.getWidth();
                    double height = responsiveRoot.getHeight();
                    if (width <= 0 || height <= 0) {
                        return 1.0;
                    }
                    return Math.min(width / GameSettings.WIDTH, height / GameSettings.HEIGHT);
                },
                responsiveRoot.widthProperty(),
                responsiveRoot.heightProperty()
        );

        scaledContent.scaleXProperty().bind(scaleBinding);
        scaledContent.scaleYProperty().bind(scaleBinding);
    }

    private void setContent(Parent content) {
        scaledContent.getChildren().setAll(content);
    }

    private void installWindowShortcuts(Scene scene) {
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            boolean fullscreenToggle = event.getCode() == KeyCode.F11
                    || (event.getCode() == KeyCode.ENTER && event.isAltDown());

            if (fullscreenToggle) {
                stage.setFullScreen(!stage.isFullScreen());
                event.consume();
                return;
            }

            if (event.getCode() == KeyCode.ESCAPE && stage.isFullScreen()) {
                stage.setFullScreen(false);
                event.consume();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
