package game;

import game.config.GameSettings;
import game.ui.GamePanel;
import game.ui.MenuPanel;
import javafx.application.Application;
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
        MenuPanel menu = new MenuPanel(this::startGame, stage::close);
        Scene scene = new Scene(menu, GameSettings.WIDTH, GameSettings.HEIGHT);
        stage.setScene(scene);
    }

    public void startGame() {
        GamePanel gamePanel = new GamePanel(this::startGame, this::showMenu);
        Scene gameScene = new Scene(gamePanel, GameSettings.WIDTH, GameSettings.HEIGHT);
        stage.setScene(gameScene);
        gamePanel.bindInput(gameScene);
        gamePanel.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
