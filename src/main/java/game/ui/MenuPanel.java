package game.ui;

import game.config.GameSettings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import game.core.SoundManager;

public class MenuPanel extends VBox {

    public MenuPanel(Runnable onStart, Runnable onExit) {
        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        setAlignment(Pos.CENTER);
        
        // เพิ่ม Spacing โดยรวมระหว่างบล็อกข้อความกับปุ่ม
        setSpacing(30); 
        
        // --- 1. ตั้งค่าภาพพื้นหลังหลัก (เหมือนเดิม) ---
        String bgImagePath;
        try {
            bgImagePath = getClass().getResource("/menu_bg.png").toExternalForm();
            setStyle(
                    "-fx-background-image: url('" + bgImagePath + "'); "
                    + "-fx-background-size: cover; "
                    + "-fx-background-position: center center; "
                    + "-fx-padding: 24;"
            );
        } catch (NullPointerException e) {
            System.err.println("Warning: Background image not found! Using fallback color.");
            setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #0d1422 0%, #192a3d 45%, #102334 100%); "
                    + "-fx-padding: 24;"
            );
        }

        // --- 2. สร้างคอนเทนเนอร์สำหรับข้อความ (เพื่อทำกรอบโปร่งแสง) ---
        VBox textContainer = new VBox();
        textContainer.setAlignment(Pos.CENTER);
        textContainer.setSpacing(10); // Spacing ระหว่างบรรทัดข้อความ
        textContainer.setMaxWidth(GameSettings.WIDTH * 0.8); // กำหนดความกว้างกรอบไม่ให้เต็มจอ

        // ตั้งค่าสไตล์กรอบโปร่งแสง (rgba โดย 0.6 คือความเข้มข้นของสีดำ 60%)
        textContainer.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.6); " + // สีดำโปร่งแสง
                "-fx-background-radius: 20; " +                  // ขอบมน
                "-fx-padding: 40; " +                            // ระยะห่างข้างในกรอบ
                "-fx-border-color: rgba(255, 255, 255, 0.1); " + // ขอบนอกสีขาวจางๆ
                "-fx-border-radius: 20; " +
                "-fx-border-width: 1;"
        );

        // --- 3. สร้างข้อความต่างๆ (เหมือนเดิม แต่เอา Tip มาใส่รวมที่นี่) ---
        Label title = new Label("GUN MAYHEM ARENA");
        title.setTextFill(Color.web("#fff4af"));
        title.setFont(Font.font("Impact", FontWeight.NORMAL, 56));

        Label subtitle = new Label("Local PvP Knockback Battle");
        subtitle.setTextFill(Color.web("#d5e8ff"));
        subtitle.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        Label controls = new Label("P1: A/D + W + SPACE    |    P2: Arrows + UP + ENTER");
        controls.setTextFill(Color.web("#b8d3f5"));
        controls.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));
        
        Label tip = new Label("First to " + GameSettings.ROUND_WINS_TO_MATCH + " rounds wins.");
        tip.setTextFill(Color.web("#c7daf8"));
        tip.setFont(Font.font("Consolas", FontWeight.NORMAL, 13));

        // ใส่ข้อความทั้งหมดลงใน textContainer
        textContainer.getChildren().addAll(title, subtitle, controls, tip);

        SoundManager.getInstance().playMenuBgm(); 

        // --- 4. สร้างปุ่ม (เหมือนเดิม อยู่ข้างนอกกรอบข้อความ) ---
        Button start = new Button("START");
        start.setPrefWidth(220);
        styleButton(start, "#3c8cff", "#1f5ec9");
        start.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            onStart.run();
        });

        Button exit = new Button("EXIT");
        exit.setPrefWidth(220);
        styleButton(exit, "#5a6577", "#3a4354");
        exit.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            onExit.run();
        });

        // --- 5. เพิ่มส่วนประกอบหลักลงใน MenuPanel (VBox หลัก) ---
        // ใส่ textContainer และปุ่มต่างๆ
        getChildren().addAll(textContainer, start, exit);
    }

    private static void styleButton(Button button, String top, String bottom) {
        button.setFocusTraversable(false);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Impact", FontWeight.NORMAL, 30));
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + top + ", " + bottom + "); "
                        + "-fx-background-radius: 10; "
                        + "-fx-padding: 8 18 8 18;"
        );
    }
}