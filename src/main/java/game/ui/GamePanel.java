package game.ui;

import game.config.GameSettings;
import game.core.PlatformSurface;
import game.core.Renderable;
import game.core.Updatable;
import game.entities.Bullet;
import game.entities.Player;
import game.entities.PlayerOne;
import game.entities.PlayerTwo;
import game.entities.weapons.Gun;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GamePanel extends StackPane {
    private static final int INITIAL_LIVES = 3;
    private static final double SKY_RESPAWN_Y_MIN = 24.0;
    private static final double SKY_RESPAWN_Y_MAX = 160.0;
    private static final double SIDE_RESPAWN_MARGIN = 48.0;

    private final Runnable onRematch;
    private final Runnable onBackToMenu;

    private final Canvas canvas = new Canvas(GameSettings.WIDTH, GameSettings.HEIGHT);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final Image mapImage = new Image(GamePanel.class.getResourceAsStream("/Map.png"));

    private static final double MAP_SOURCE_WIDTH = 598.0;
    private static final double MAP_SOURCE_HEIGHT = 348.0;

    private final PlayerOne p1;
    private final PlayerTwo p2;

    private final List<Bullet> bullets = new ArrayList<>();
    private final List<PlatformSurface> worldSurfaces = List.of(
            new PlatformSurface(sx(106), sy(146), sw(148), sh(12), true),
            new PlatformSurface(sx(309), sy(146), sw(145), sh(12), true),
            new PlatformSurface(sx(62), sy(214), sw(493), sh(14), true),
            new PlatformSurface(sx(107), sy(285), sw(88), sh(12), true),
            new PlatformSurface(sx(400), sy(285), sw(89), sh(12), true),
            new PlatformSurface(sx(85), sy(327), sw(434), sh(14), true)
    );
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Random random = new Random();
    private final Button pauseButton = new Button("Pause");
    private VBox pauseModal;

    private boolean gameOver;
    private boolean paused;
    private long lastFrameNanos;
    private int p1Lives = INITIAL_LIVES;
    private int p2Lives = INITIAL_LIVES;

    private final AnimationTimer gameLoop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (lastFrameNanos == 0L) {
                lastFrameNanos = now;
                return;
            }
            double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
            lastFrameNanos = now;

            updateGame(deltaSeconds);
            renderGame();
        }
    };

    public GamePanel(Gun p1Weapon, Gun p2Weapon, Runnable onRematch, Runnable onBackToMenu) {
        this.onRematch = onRematch;
        this.onBackToMenu = onBackToMenu;

        p1 = new PlayerOne(sx(100), sy(214) - GameSettings.PLAYER_HEIGHT, "/Player.png", List.of());
        p2 = new PlayerTwo(sx(500), sy(214) - GameSettings.PLAYER_HEIGHT, "/Player.png", List.of());
        p1.equipPermanentGun(p1Weapon);
        p2.equipPermanentGun(p2Weapon);

        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        getChildren().add(canvas);
        setupPauseUi();

        gameLoop.start();
    }

    public void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            if (event.getCode() == KeyCode.ESCAPE) {
                togglePause();
                return;
            }

            if (paused) {
                return;
            }

            if (gameOver) {
                return;
            }

            if (event.getCode() == KeyCode.SPACE) {
                performAction(p1, p2);
            } else if (event.getCode() == KeyCode.ENTER) {
                performAction(p2, p1);
            } else if (event.getCode() == KeyCode.W) {
                p1.jump();
            } else if (event.getCode() == KeyCode.UP) {
                p2.jump();
            } else if (event.getCode() == KeyCode.S) {
                if (canDropToLowerPlatform(p1)) {
                    p1.requestDropThrough(System.currentTimeMillis());
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (canDropToLowerPlatform(p2)) {
                    p2.requestDropThrough(System.currentTimeMillis());
                }
            }
        });

        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }

    private void updateGame(double deltaSeconds) {
        if (paused) {
            return;
        }

        updateMovement();

        List<Updatable> updatables = new ArrayList<>();
        updatables.add(p1);
        updatables.add(p2);
        updatables.addAll(bullets);
        for (Updatable updatable : updatables) {
            updatable.update(deltaSeconds);
        }

        long now = System.currentTimeMillis();
        p1.resolveCollisions(worldSurfaces, now);
        p2.resolveCollisions(worldSurfaces, now);
        p1.clampX(0, GameSettings.WIDTH - GameSettings.PLAYER_WIDTH);
        p2.clampX(0, GameSettings.WIDTH - GameSettings.PLAYER_WIDTH);

        handleBulletHits();
        bullets.removeIf(bullet -> !bullet.isActive());
        handleFallDeaths();
    }

    private void updateMovement() {
        double p1Horizontal = axis(pressedKeys.contains(KeyCode.A), pressedKeys.contains(KeyCode.D));
        p1.setHorizontalInput(p1Horizontal);

        double p2Horizontal = axis(pressedKeys.contains(KeyCode.LEFT), pressedKeys.contains(KeyCode.RIGHT));
        p2.setHorizontalInput(p2Horizontal);
    }

    private double axis(boolean negative, boolean positive) {
        if (negative == positive) {
            return 0.0;
        }
        return positive ? 1.0 : -1.0;
    }

    private void performAction(Player attacker, Player defender) {
        long now = System.currentTimeMillis();
        if (!attacker.canAction(now)) {
            return;
        }

        if (attacker.hasGun(now)) {
            bullets.addAll(attacker.shoot());
            attacker.setActionCooldown(now, attacker.getShootCooldownMillis(now));
            return;
        }

        if (attacker.isMeleeHit(defender)) {
            defender.applyKnockback(attacker.getFacingDirection() * GameSettings.MELEE_FORCE, GameSettings.MELEE_VERTICAL_FORCE);
        }
        attacker.setActionCooldown(now, GameSettings.MELEE_COOLDOWN_MS);
    }

    private void handleBulletHits() {
        for (Bullet bullet : bullets) {
            if (!bullet.isActive()) {
                continue;
            }

            Player owner = bullet.getOwner();
            Player target = owner == p1 ? p2 : p1;
            if (bullet.getBounds().intersects(target.getBounds())) {
                target.applyKnockback(bullet.getImpactForceX(), bullet.getImpactForceY());
                bullet.deactivate();
            }
        }
    }

    private boolean canDropToLowerPlatform(Player player) {
        if (!player.isOnGround()) {
            return false;
        }

        var playerBounds = player.getBounds();
        double playerBottom = playerBounds.getMaxY();
        double nearestPlatformBelowTop = Double.POSITIVE_INFINITY;

        for (PlatformSurface surface : worldSurfaces) {
            var bounds = surface.getBounds();
            boolean horizontalOverlap = bounds.getMaxX() > playerBounds.getMinX() + 2
                    && bounds.getMinX() < playerBounds.getMaxX() - 2;
            if (!horizontalOverlap) {
                continue;
            }
            if (bounds.getMinY() <= playerBottom + 6) {
                continue;
            }

            nearestPlatformBelowTop = Math.min(nearestPlatformBelowTop, bounds.getMinY());
        }

        if (nearestPlatformBelowTop == Double.POSITIVE_INFINITY) {
            return false;
        }

        return nearestPlatformBelowTop + GameSettings.PLAYER_HEIGHT <= GameSettings.WORLD_FLOOR_Y;
    }

    private void handleFallDeaths() {
        if (gameOver) {
            return;
        }

        if (isOutOfMap(p1)) {
            p1Lives--;
            if (p1Lives <= 0) {
                finishGame("Player 2");
                return;
            }
            respawnPlayerFromSky(p1);
        }

        if (gameOver) {
            return;
        }

        if (isOutOfMap(p2)) {
            p2Lives--;
            if (p2Lives <= 0) {
                finishGame("Player 1");
                return;
            }
            respawnPlayerFromSky(p2);
        }
    }

    private boolean isOutOfMap(Player player) {
        var b = player.getBounds();
        return b.getMinY() > GameSettings.WORLD_FLOOR_Y;
    }

    private void renderGame() {
        gc.setFill(Color.web("#d9ecff"));
        gc.fillRect(0, 0, GameSettings.WIDTH, GameSettings.HEIGHT);
        gc.drawImage(mapImage, 0, 0, GameSettings.WIDTH, GameSettings.HEIGHT);
        renderPlatformGuides();

        gc.setStroke(Color.web("#2f3541"));
        gc.strokeRect(1, 1, GameSettings.WIDTH - 2, GameSettings.HEIGHT - 2);

        List<Renderable> renderables = new ArrayList<>();
        renderables.add(p1);
        renderables.add(p2);
        renderables.addAll(bullets);
        for (Renderable renderable : renderables) {
            renderable.render(gc);
        }

        renderHud();
    }

    private void renderPlatformGuides() {
        gc.setFill(Color.web("#2f7cff", 0.55));
        gc.setStroke(Color.web("#0f3fa1", 0.9));
        gc.setLineWidth(2.0);
        for (PlatformSurface surface : worldSurfaces) {
            var b = surface.getBounds();
            gc.fillRoundRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight(), 8, 8);
            gc.strokeRoundRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight(), 8, 8);
        }
    }

    private void renderHud() {
        long now = System.currentTimeMillis();
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        gc.fillText("P1 HP: " + p1Lives + "  Gun: " + p1.getEquippedGun(now).label(), 20, 26);
        gc.fillText("P2 HP: " + p2Lives + "  Gun: " + p2.getEquippedGun(now).label(), GameSettings.WIDTH - 290, 26);
    }

    private void finishGame(String winner) {
        if (gameOver) {
            return;
        }
        gameOver = true;
        paused = false;
        gameLoop.stop();
        pauseButton.setVisible(false);
        if (pauseModal != null) {
            pauseModal.setVisible(false);
        }

        Label title = new Label(winner + " Wins!");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 32));

        Button restart = new Button("Restart");
        restart.setPrefWidth(180);
        restart.setOnAction(event -> onRematch.run());

        Button backToMenu = new Button("Return to Menu");
        backToMenu.setPrefWidth(180);
        backToMenu.setOnAction(event -> {
            shutdownGameSystems();
            onBackToMenu.run();
        });

        VBox modal = new VBox(14, title, restart, backToMenu);
        modal.setAlignment(Pos.CENTER);
        modal.setMaxWidth(350);
        modal.setMaxHeight(220);
        modal.setStyle("-fx-background-color: rgba(0,0,0,0.78); -fx-padding: 24; -fx-background-radius: 10;");
        getChildren().add(modal);
    }

    private static double sx(double mapX) {
        return (mapX / MAP_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    private static double sy(double mapY) {
        return (mapY / MAP_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }

    private static double sw(double mapWidth) {
        return (mapWidth / MAP_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    private static double sh(double mapHeight) {
        return (mapHeight / MAP_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }

    private void respawnPlayerFromSky(Player player) {
        double minX = SIDE_RESPAWN_MARGIN;
        double maxX = GameSettings.WIDTH - GameSettings.PLAYER_WIDTH - SIDE_RESPAWN_MARGIN;
        double spawnX = minX + random.nextDouble() * (maxX - minX);
        double spawnY = -(SKY_RESPAWN_Y_MIN + random.nextDouble() * (SKY_RESPAWN_Y_MAX - SKY_RESPAWN_Y_MIN));
        player.respawnFromSky(spawnX, spawnY);
    }

    private void setupPauseUi() {
        pauseButton.setFocusTraversable(false);
        pauseButton.setOnAction(event -> togglePause());
        StackPane.setAlignment(pauseButton, Pos.TOP_RIGHT);
        StackPane.setMargin(pauseButton, new Insets(10, 10, 0, 0));

        Label title = new Label("Paused");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 30));

        Button cont = new Button("Continue");
        cont.setPrefWidth(220);
        cont.setOnAction(event -> setPaused(false));

        Button restart = new Button("Restart");
        restart.setPrefWidth(220);
        restart.setOnAction(event -> {
            shutdownGameSystems();
            onRematch.run();
        });

        Button menu = new Button("Return to Menu");
        menu.setPrefWidth(220);
        menu.setOnAction(event -> {
            shutdownGameSystems();
            onBackToMenu.run();
        });

        pauseModal = new VBox(14, title, cont, restart, menu);
        pauseModal.setAlignment(Pos.CENTER);
        pauseModal.setVisible(false);
        pauseModal.setMaxWidth(360);
        pauseModal.setMaxHeight(260);
        pauseModal.setStyle("-fx-background-color: rgba(0,0,0,0.82); -fx-padding: 24; -fx-background-radius: 10;");

        getChildren().addAll(pauseModal, pauseButton);
    }

    private void togglePause() {
        if (gameOver) {
            return;
        }
        setPaused(!paused);
    }

    private void setPaused(boolean pauseValue) {
        paused = pauseValue;
        if (paused) {
            pressedKeys.clear();
        }
        if (pauseModal != null) {
            pauseModal.setVisible(paused);
        }
    }

    private void shutdownGameSystems() {
        gameLoop.stop();
    }
}
