package game;

import game.config.GameSettings;
import game.entities.weapons.Gun;
import game.ui.GamePanel;
import game.ui.MenuPanel;
import game.ui.WeaponSelectPanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameMain extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("PvP Knockback Game");
        stage.setResizable(false);
        showMenu();
        stage.show();
    }

    public void showMenu() {
        MenuPanel menu = new MenuPanel(this::showWeaponSelect, stage::close);
        Scene scene = new Scene(menu, GameSettings.WIDTH, GameSettings.HEIGHT);
        stage.setScene(scene);
    }

    public void showWeaponSelect() {
        WeaponSelectPanel weaponSelect = new WeaponSelectPanel(this::startGame, this::showMenu);
        Scene scene = new Scene(weaponSelect, GameSettings.WIDTH, GameSettings.HEIGHT);
        stage.setScene(scene);
        Platform.runLater(weaponSelect::requestFocus);
    }

    public void startGame(Gun p1Weapon, Gun p2Weapon) {
        GamePanel gamePanel = new GamePanel(
                p1Weapon,
                p2Weapon,
                this::showWeaponSelect,
                this::showMenu
        );
        Scene gameScene = new Scene(gamePanel, GameSettings.WIDTH, GameSettings.HEIGHT);
        stage.setScene(gameScene);
        gamePanel.bindInput(gameScene);
        gamePanel.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
