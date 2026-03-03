package game.ui;

import game.config.GameSettings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MenuPanel extends VBox {

    public MenuPanel(Runnable onStart, Runnable onExit) {
        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        setAlignment(Pos.CENTER);
        setSpacing(18);
        setStyle("-fx-background-color: linear-gradient(to bottom, #10151f, #0f2633);");

        Label title = new Label("Gun Mayhem Style");
        title.setTextFill(Color.WHITESMOKE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 36));

        Button start = new Button("START");
        start.setPrefWidth(220);
        start.setOnAction(event -> onStart.run());

        Button exit = new Button("EXIT");
        exit.setPrefWidth(220);
        exit.setOnAction(event -> onExit.run());

        getChildren().addAll(title, start, exit);
    }
}
