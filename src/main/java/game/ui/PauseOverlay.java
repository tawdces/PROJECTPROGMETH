package game.ui;

import game.logic.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

final class PauseOverlay {
    private static final double VOLUME_PERCENT_MIN = 0.0;
    private static final double VOLUME_PERCENT_MAX = 100.0;

    private final Button pauseButton = new Button("Pause");
    private final VBox pauseModal;

    PauseOverlay(
            StackPane host,
            Runnable onPauseToggle,
            Runnable onContinue,
            Runnable onRestart,
            Runnable onBackToMenu
    ) {
        SoundManager soundManager = SoundManager.getInstance();

        pauseButton.setFocusTraversable(false);
        pauseButton.setPrefWidth(100);
        styleMenuButton(pauseButton, "#4c5f80", "#2f3b53");
        pauseButton.setOnAction(event -> {
            soundManager.playEffect("click");
            onPauseToggle.run();
        });
        StackPane.setAlignment(pauseButton, Pos.TOP_RIGHT);
        StackPane.setMargin(pauseButton, new Insets(12, 12, 0, 0));

        Label title = new Label("Paused");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Impact", FontWeight.NORMAL, 38));

        Label musicVolumeLabel = new Label();
        musicVolumeLabel.setId("pauseMusicVolumeLabel");
        stylePauseAudioLabel(musicVolumeLabel);

        Slider musicVolumeSlider = createPauseVolumeSlider(soundManager.getMusicVolume());
        musicVolumeSlider.setId("pauseMusicVolumeSlider");
        musicVolumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            double volume = newValue.doubleValue() / 100.0;
            soundManager.setMusicVolume(volume);
            musicVolumeLabel.setText(volumeText("MUSIC", newValue.doubleValue()));
        });
        musicVolumeLabel.setText(volumeText("MUSIC", musicVolumeSlider.getValue()));

        Label effectVolumeLabel = new Label();
        effectVolumeLabel.setId("pauseEffectVolumeLabel");
        stylePauseAudioLabel(effectVolumeLabel);

        Slider effectVolumeSlider = createPauseVolumeSlider(soundManager.getEffectVolume());
        effectVolumeSlider.setId("pauseEffectVolumeSlider");
        effectVolumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            double volume = newValue.doubleValue() / 100.0;
            soundManager.setEffectVolume(volume);
            effectVolumeLabel.setText(volumeText("EFFECT", newValue.doubleValue()));
        });
        effectVolumeSlider.setOnMouseReleased(event -> soundManager.playEffect("click"));
        effectVolumeLabel.setText(volumeText("EFFECT", effectVolumeSlider.getValue()));

        HBox musicRow = new HBox(12, musicVolumeLabel, musicVolumeSlider);
        musicRow.setAlignment(Pos.CENTER);

        HBox effectRow = new HBox(12, effectVolumeLabel, effectVolumeSlider);
        effectRow.setAlignment(Pos.CENTER);

        VBox audioControls = new VBox(8, musicRow, effectRow);
        audioControls.setAlignment(Pos.CENTER);
        audioControls.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); "
                        + "-fx-background-radius: 10; "
                        + "-fx-padding: 10 12 10 12;"
        );

        Button cont = new Button("Continue");
        cont.setPrefWidth(220);
        styleMenuButton(cont, "#3c8cff", "#1f5ec9");
        cont.setOnAction(event -> {
            soundManager.playEffect("click");
            onContinue.run();
        });

        Button restart = new Button("Restart Match");
        restart.setPrefWidth(220);
        styleMenuButton(restart, "#70839a", "#4d5f74");
        restart.setOnAction(event -> {
            soundManager.playEffect("click");
            onRestart.run();
        });

        Button menu = new Button("Return to Menu");
        menu.setPrefWidth(220);
        styleMenuButton(menu, "#3a4354", "#252d39");
        menu.setOnAction(event -> {
            soundManager.playEffect("click");
            onBackToMenu.run();
        });

        pauseModal = new VBox(14, title, audioControls, cont, restart, menu);
        pauseModal.setAlignment(Pos.CENTER);
        pauseModal.setVisible(false);
        pauseModal.setMaxWidth(420);
        pauseModal.setMaxHeight(360);
        pauseModal.setStyle("-fx-background-color: rgba(0,0,0,0.84); -fx-padding: 24; -fx-background-radius: 14;");

        host.getChildren().addAll(pauseModal, pauseButton);
    }

    void showPaused(boolean paused) {
        pauseModal.setVisible(paused);
    }

    void hideAll() {
        pauseButton.setVisible(false);
        pauseModal.setVisible(false);
    }

    private static void styleMenuButton(Button button, String top, String bottom) {
        button.setFocusTraversable(false);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Impact", FontWeight.NORMAL, 22));
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + top + ", " + bottom + "); "
                        + "-fx-background-radius: 10; "
                        + "-fx-padding: 8 16 8 16;"
        );
    }

    private static Slider createPauseVolumeSlider(double volume) {
        Slider slider = new Slider(VOLUME_PERCENT_MIN, VOLUME_PERCENT_MAX, volume * 100.0);
        slider.setFocusTraversable(false);
        slider.setPrefWidth(220);
        slider.setBlockIncrement(1.0);
        return slider;
    }

    private static void stylePauseAudioLabel(Label label) {
        label.setTextFill(Color.web("#ffe8a0"));
        label.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
    }

    private static String volumeText(String channel, double valuePercent) {
        int percent = (int) Math.round(Math.max(VOLUME_PERCENT_MIN, Math.min(VOLUME_PERCENT_MAX, valuePercent)));
        return channel + ": " + percent + "%";
    }
}
