package game.ui;

import game.config.GameSettings;
import game.core.MatchTimerService;
import game.core.PlatformSurface;
import game.core.Renderable;
import game.core.Updatable;
import game.entities.Bullet;
import game.entities.Enemy;
import game.entities.Player;
import game.entities.WeaponBox;
import game.entities.WeaponType;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GamePanel extends StackPane {

    private final Runnable onRestart;
    private final Runnable onBackToMenu;

    private final Canvas canvas = new Canvas(GameSettings.WIDTH, GameSettings.HEIGHT);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final Image mapImage = new Image(GamePanel.class.getResourceAsStream("/Map.png"));

    private static final double MAP_SOURCE_WIDTH = 598.0;
    private static final double MAP_SOURCE_HEIGHT = 348.0;

    private final Player p1 = new Player(
            sx(100),
            sy(214) - GameSettings.PLAYER_HEIGHT,
            "P1",
            Color.DODGERBLUE,
            1,
            "/Player.png",
            List.of()
    );
    private final Enemy p2 = new Enemy(
            sx(500),
            sy(214) - GameSettings.PLAYER_HEIGHT,
            "/Player.png",
            List.of()
    );

    private final List<Bullet> bullets = new ArrayList<>();
    private final List<WeaponBox> weaponBoxes = new ArrayList<>();
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
    private final MatchTimerService matchTimer = new MatchTimerService();
    private final List<WeaponType> pickupWeapons = List.of(
            WeaponType.PISTOL,
            WeaponType.RIFLE,
            WeaponType.MACHINE_GUN,
            WeaponType.SHOTGUN
    );

    private final long gameStartMillis = System.currentTimeMillis();
    private long lastDropMillis = gameStartMillis;
    private boolean firstDropDone;
    private boolean gameOver;

    private long lastFrameNanos;

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

    public GamePanel(Runnable onRestart, Runnable onBackToMenu) {
        this.onRestart = onRestart;
        this.onBackToMenu = onBackToMenu;

        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        getChildren().add(canvas);

        matchTimer.start();
        gameLoop.start();
    }

    public void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

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
                tryPickup(p1);
                p1.requestDropThrough(System.currentTimeMillis());
            } else if (event.getCode() == KeyCode.DOWN) {
                tryPickup(p2);
                p2.requestDropThrough(System.currentTimeMillis());
            }
        });

        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }

    private void updateGame(double deltaSeconds) {
        updateMovement();

        List<Updatable> updatables = new ArrayList<>();
        updatables.add(p1);
        updatables.add(p2);
        updatables.addAll(bullets);
        updatables.addAll(weaponBoxes);
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
        weaponBoxes.removeIf(box -> !box.isActive());

        spawnWeaponBoxesByTime();
        checkLoseConditions();
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

    private void spawnWeaponBoxesByTime() {
        long now = System.currentTimeMillis();

        if (!firstDropDone && now - gameStartMillis >= GameSettings.FIRST_DROP_DELAY_MS) {
            spawnWeaponPair();
            firstDropDone = true;
            lastDropMillis = now;
            return;
        }

        if (firstDropDone && now - lastDropMillis >= GameSettings.NEXT_DROP_INTERVAL_MS) {
            spawnWeaponPair();
            lastDropMillis = now;
        }
    }

    private void spawnWeaponPair() {
        double x1 = randomBoxX();
        double x2 = randomBoxX();
        while (Math.abs(x1 - x2) < GameSettings.BOX_SIZE * 2.4) {
            x2 = randomBoxX();
        }

        weaponBoxes.add(new WeaponBox(x1, findLandingY(x1), randomWeaponType()));
        weaponBoxes.add(new WeaponBox(x2, findLandingY(x2), randomWeaponType()));
    }

    private WeaponType randomWeaponType() {
        return pickupWeapons.get(random.nextInt(pickupWeapons.size()));
    }

    private double randomBoxX() {
        PlatformSurface surface = worldSurfaces.get(random.nextInt(worldSurfaces.size()));
        Rectangle2D b = surface.getBounds();
        double minX = b.getMinX() + 20;
        double maxX = b.getMaxX() - GameSettings.BOX_SIZE - 20;
        if (maxX <= minX) {
            return b.getMinX() + 4;
        }
        return minX + random.nextDouble() * (maxX - minX);
    }

    private double findLandingY(double x) {
        double boxCenterX = x + (GameSettings.BOX_SIZE / 2.0);
        double landingTop = GameSettings.WORLD_FLOOR_Y;
        for (PlatformSurface surface : worldSurfaces) {
            Rectangle2D b = surface.getBounds();
            if (boxCenterX >= b.getMinX() && boxCenterX <= b.getMaxX()) {
                landingTop = Math.min(landingTop, b.getMinY());
            }
        }
        return landingTop - GameSettings.BOX_SIZE;
    }

    private void tryPickup(Player player) {
        long now = System.currentTimeMillis();
        for (WeaponBox box : weaponBoxes) {
            if (box.isActive() && box.getBounds().intersects(player.getBounds())) {
                box.deactivate();
                player.equipGun(box.getWeaponType(), now);
                return;
            }
        }
    }

    private void checkLoseConditions() {
        if (isOutOfMap(p1)) {
            finishGame("Player 2");
            return;
        }
        if (isOutOfMap(p2)) {
            finishGame("Player 1");
        }
    }

    private boolean isOutOfMap(Player player) {
        Rectangle2D b = player.getBounds();
        return b.getMinY() > GameSettings.WORLD_FLOOR_Y;
    }

    private void renderGame() {
        gc.setFill(Color.web("#d9ecff"));
        gc.fillRect(0, 0, GameSettings.WIDTH, GameSettings.HEIGHT);
        gc.drawImage(mapImage, 0, 0, GameSettings.WIDTH, GameSettings.HEIGHT);

        gc.setStroke(Color.web("#2f3541"));
        gc.strokeRect(1, 1, GameSettings.WIDTH - 2, GameSettings.HEIGHT - 2);

        List<Renderable> renderables = new ArrayList<>();
        renderables.addAll(weaponBoxes);
        renderables.add(p1);
        renderables.add(p2);
        renderables.addAll(bullets);
        for (Renderable renderable : renderables) {
            renderable.render(gc);
        }

        renderHud();
    }

    private void renderHud() {
        long now = System.currentTimeMillis();
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        gc.fillText("P1 Gun: " + formatGunStatus(p1, now), 20, 26);
        gc.fillText("Time: " + String.format("%.1fs", matchTimer.getElapsedSeconds()), GameSettings.WIDTH / 2.0 - 52, 26);
        gc.fillText("P2 Gun: " + formatGunStatus(p2, now), GameSettings.WIDTH - 260, 26);
    }

    private String formatGunStatus(Player player, long now) {
        WeaponType weapon = player.getEquippedWeapon(now);
        long remain = player.getGunRemainingMillis(now);
        if (weapon == WeaponType.NONE || remain <= 0) {
            return "OFF";
        }
        return weapon.label() + " " + String.format("%.1fs", remain / 1000.0);
    }

    private void finishGame(String winner) {
        if (gameOver) {
            return;
        }
        gameOver = true;
        gameLoop.stop();
        matchTimer.stop();

        Label title = new Label(winner + " Wins!");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 32));

        Button restart = new Button("Restart");
        restart.setPrefWidth(180);
        restart.setOnAction(event -> {
            matchTimer.stop();
            onRestart.run();
        });

        Button backToMenu = new Button("Back to Menu");
        backToMenu.setPrefWidth(180);
        backToMenu.setOnAction(event -> {
            matchTimer.stop();
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
}
