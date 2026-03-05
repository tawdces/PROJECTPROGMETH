package game;

import game.config.GameSettings;
import game.entities.weapons.Gun;
import game.map.GameMap;
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

/**
 * Represents the game main.
 */
public class GameMain extends Application {

    /**
     * Internal state field for stage.
     */
    private Stage stage;
    /**
     * Internal state field for app scene.
     */
    private Scene appScene;
    /**
     * Internal state field for scaled content.
     */
    private final Group scaledContent = new Group();
    /**
     * Internal state field for responsive root.
     */
    private final StackPane responsiveRoot = new StackPane(scaledContent);
    /**
     * Internal state field for selected map.
     */
    private GameMap selectedMap = GameMap.defaultMap();

    /**
     * Creates the application bootstrap instance.
     */
    public GameMain() {
    }

    /**
     * Starts this component.
     *
     * @param primaryStage parameter value
     */
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

    /**
     * Shows the menu.
     */
    public void showMenu() {
        MenuPanel menu = new MenuPanel(this::showMapSelect, stage::close);
        setContent(menu);
    }

    /**
     * Shows the map select.
     */
    public void showMapSelect() {
        MapSelectPanel mapSelect = new MapSelectPanel(this::showWeaponSelect, this::showMenu);
        setContent(mapSelect);
        Platform.runLater(mapSelect::requestFocus);
    }

    /**
     * Shows the weapon select.
     *
     * @param map parameter value
     */
    public void showWeaponSelect(GameMap map) {
        selectedMap = map == null ? GameMap.defaultMap() : map;
        WeaponSelectPanel weaponSelect = new WeaponSelectPanel(this::startGame, this::showMapSelect);
        setContent(weaponSelect);
        Platform.runLater(weaponSelect::requestFocus);
    }

    /**
     * Executes start game.
     *
     * @param p1Weapon parameter value
     * @param p2Weapon parameter value
     */
    public void startGame(Gun p1Weapon, Gun p2Weapon) {
        GamePanel gamePanel = new GamePanel(
                selectedMap,
                p1Weapon,
                p2Weapon,
                () -> showWeaponSelect(selectedMap),
                this::showMenu
        );
        setContent(gamePanel);
        gamePanel.bindInput(appScene);
        Platform.runLater(gamePanel::requestFocus);
    }

    /**
     * Sets internal up responsive root.
     */
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

    /**
     * Sets internal content.
     *
     * @param content parameter value
     */
    private void setContent(Parent content) {
        scaledContent.getChildren().setAll(content);
    }

    /**
     * Internal helper for install window shortcuts.
     *
     * @param scene parameter value
     */
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

    /**
     * Launches the application entry point.
     *
     * @param args parameter value
     */
    public static void main(String[] args) {
        launch(args);
    }
}
