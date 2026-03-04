package game.ui;

import game.config.GameSettings;
import game.core.PlatformSurface;
import game.core.Renderable;
import game.core.SoundManager;
import game.core.Updatable;
import game.entities.Bullet;
import game.entities.Player;
import game.entities.PlayerOne;
import game.entities.PlayerTwo;
import game.entities.weapons.Gun;
import game.entities.weapons.GunRegistry;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GamePanel extends StackPane {
    private static final double SKY_RESPAWN_Y_MIN = 24.0;
    private static final double SKY_RESPAWN_Y_MAX = 160.0;
    private static final double SIDE_RESPAWN_MARGIN = 48.0;
    private static final double CAMERA_MIN_ZOOM = 0.92;
    private static final double CAMERA_MAX_ZOOM = 1.9;
    private static final double CAMERA_MARGIN_X = 260.0;
    private static final double CAMERA_MARGIN_Y = 180.0;
    private static final double CAMERA_LERP_SPEED = 8.0;
    private static final double MAP_RENDER_EXTEND_MARGIN = GameSettings.BLAST_ZONE_MARGIN + 24.0;
    private static final Image EMPTY_IMAGE = new WritableImage(1, 1);

    private final Runnable onRematch;
    private final Runnable onBackToMenu;

    private final Canvas canvas = new Canvas(GameSettings.WIDTH, GameSettings.HEIGHT);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final Image selectedMapImage;
    private final Image bulletHitImage = loadTransparentImage("/Bullet_hit.png");
    private final Image bloodImage = loadTransparentImage("/Blood.png");
    
    
    private final Image barrelImage = loadTransparentImage("/barrel.png");
    private final Image explosionImage = loadTransparentImage("/explosion.png");

    private static final double MAP_SOURCE_WIDTH = 598.0;
    private static final double MAP_SOURCE_HEIGHT = 348.0;

    private final PlayerOne p1;
    private final PlayerTwo p2;

    private final List<Bullet> bullets = new ArrayList<>();
    private final List<WeaponDrop> weaponDrops = new ArrayList<>();
    private final List<ExplosiveBarrel> barrels = new ArrayList<>(); 
    private final List<HitEffect> hitEffects = new ArrayList<>();
    private final List<PlatformSurface> worldSurfaces;
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Random random = new Random();
    private final Button pauseButton = new Button("Pause");
    private VBox pauseModal;

    private boolean gameOver;
    private boolean paused;
    private long lastFrameNanos;
    private int p1Stocks = GameSettings.STOCKS_PER_ROUND;
    private int p2Stocks = GameSettings.STOCKS_PER_ROUND;
    private int p1RoundWins;
    private int p2RoundWins;
    private int roundNumber = 1;
    private double cameraX;
    private double cameraY;
    private double cameraZoom = CAMERA_MIN_ZOOM;
    private long nextGunDropAtMillis = System.currentTimeMillis() + GameSettings.FIRST_DROP_DELAY_MS;
    private long nextBarrelDropAtMillis;
    private long freezeUntilMillis;
    private long pendingNextRoundAtMillis;
    private String centerBannerText = "";
    private long centerBannerUntilMillis;
    private long shakeUntilMillis;
    private double shakeStrength;

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

    public GamePanel(String mapResourcePath, Gun p1Weapon, Gun p2Weapon, Runnable onRematch, Runnable onBackToMenu) {
        this.onRematch = onRematch;
        this.onBackToMenu = onBackToMenu;
        this.selectedMapImage = loadImage(mapResourcePath);
        this.worldSurfaces = createSurfacesForMap(mapResourcePath);

        p1 = new PlayerOne(sx(100), sy(214) - GameSettings.PLAYER_HEIGHT, "/Player.png", List.of());
        p2 = new PlayerTwo(sx(500), sy(214) - GameSettings.PLAYER_HEIGHT, "/Player.png", List.of());
        p1.equipPermanentGun(p1Weapon);
        p2.equipPermanentGun(p2Weapon);

        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        getChildren().add(canvas);
        setupPauseUi();
        prepareRound(1);

        SoundManager.getInstance().playRandomBgm(); 

        gameLoop.start();
    }

    public void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            if (event.getCode() == KeyCode.ESCAPE) {
                SoundManager.getInstance().playEffect("click");
                togglePause();
                return;
            }

            long now = System.currentTimeMillis();
            if (paused || gameOver || isCombatLocked(now)) {
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
                    p1.requestDropThrough(now);
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (canDropToLowerPlatform(p2)) {
                    p2.requestDropThrough(now);
                }
            }
        });

        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }

    private boolean isCombatLocked(long nowMillis) {
        return nowMillis < freezeUntilMillis || pendingNextRoundAtMillis > 0L;
    }

    private void updateGame(double deltaSeconds) {
        if (paused || gameOver) {
            return;
        }

        long now = System.currentTimeMillis();
        if (pendingNextRoundAtMillis > 0L && now >= pendingNextRoundAtMillis) {
            pendingNextRoundAtMillis = 0L;
            prepareRound(roundNumber + 1);
            now = System.currentTimeMillis();
        }

        if (isCombatLocked(now)) {
            p1.setHorizontalInput(0.0);
            p2.setHorizontalInput(0.0);
        } else {
            updateMovement();
        }

        List<Updatable> updatables = new ArrayList<>();
        updatables.add(p1);
        updatables.add(p2);
        updatables.addAll(bullets);
        for (Updatable updatable : updatables) {
            updatable.update(deltaSeconds);
        }

        p1.resolveCollisions(worldSurfaces, now);
        p2.resolveCollisions(worldSurfaces, now);
        removeExpiredEffects(now);
        
        
        bullets.removeIf(bullet -> !bullet.isActive());
        barrels.removeIf(barrel -> !barrel.active);

        if (!isCombatLocked(now)) {
            handleBulletHits(now);
            updateWeaponDrops(now);
            updateBarrelDrops(now);
            handleWeaponPickups(now);
            handleBlastZoneDeaths(now);
        }

        updateCamera(deltaSeconds);
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
            List<Bullet> fired = attacker.shoot();
            bullets.addAll(fired);
            if (!fired.isEmpty()) {
                SoundManager.getInstance().playEffect("shoot"); 
                double recoil = GameSettings.SHOOT_RECOIL_BASE
                        + Math.max(0, fired.size() - 1) * GameSettings.SHOOT_RECOIL_PER_BULLET;
                attacker.applyKnockback(-attacker.getFacingDirection() * recoil, GameSettings.SHOOT_RECOIL_VERTICAL_FORCE);
                triggerCameraShake(GameSettings.SCREEN_SHAKE_STRENGTH * 0.35, 70L);
            }
            attacker.setActionCooldown(now, attacker.getShootCooldownMillis(now));
            return;
        }

        
        boolean attackHit = false;
        Rectangle2D meleeHitbox = attacker.getMeleeHitbox();

        
        for (ExplosiveBarrel barrel : barrels) {
            if (barrel.active && meleeHitbox.intersects(barrel.getBounds())) {
                SoundManager.getInstance().playEffect("melee");
                triggerExplosion(barrel, now);
                attackHit = true;
                break; 
            }
        }

        
        if (!attackHit && !defender.isInvulnerable(now) && meleeHitbox.intersects(defender.getBounds())) {
            SoundManager.getInstance().playEffect("melee"); 
            defender.applyKnockback(attacker.getFacingDirection() * GameSettings.MELEE_FORCE, GameSettings.MELEE_VERTICAL_FORCE);
            addEffect("blood", bloodImage, defender.getBounds().getMinX() + 10, defender.getBounds().getMinY() + 16, 20, 20, 180L);
            triggerCameraShake(GameSettings.SCREEN_SHAKE_STRENGTH * 0.7, GameSettings.SCREEN_SHAKE_DURATION_MS);
        }
        
        attacker.setActionCooldown(now, GameSettings.MELEE_COOLDOWN_MS);
    }

    private void handleBulletHits(long now) {
        for (Bullet bullet : bullets) {
            if (!bullet.isActive()) {
                continue;
            }

            
            for (PlatformSurface surface : worldSurfaces) {
                if (bullet.getBounds().intersects(surface.getBounds())) {
                    var bulletBounds = bullet.getBounds();
                    addEffect("hit", bulletHitImage, bulletBounds.getMinX(), bulletBounds.getMinY(), 18, 18, 120L);
                    bullet.deactivate();
                    break;
                }
            }
            if (!bullet.isActive()) continue;

            
            for (ExplosiveBarrel barrel : barrels) {
                if (barrel.active && bullet.getBounds().intersects(barrel.getBounds())) {
                    bullet.deactivate();
                    triggerExplosion(barrel, now);
                    break;
                }
            }
            if (!bullet.isActive()) continue;

            
            Player owner = bullet.getOwner();
            Player target = owner == p1 ? p2 : p1;
            if (bullet.getBounds().intersects(target.getBounds())) {
                var bulletBounds = bullet.getBounds();
                addEffect("hit", bulletHitImage, bulletBounds.getMinX(), bulletBounds.getMinY(), 22, 22, 150L);
                if (!target.isInvulnerable(now)) {
                    SoundManager.getInstance().playEffect("hit"); 
                    target.applyKnockback(bullet.getImpactForceX(), bullet.getImpactForceY());
                    var targetBounds = target.getBounds();
                    addEffect(
                            "blood",
                            bloodImage,
                            targetBounds.getMinX() + Math.max(0.0, (targetBounds.getWidth() - 22.0) * 0.5),
                            targetBounds.getMinY() + Math.max(0.0, (targetBounds.getHeight() - 22.0) * 0.4),
                            22,
                            22,
                            220L
                    );
                    triggerCameraShake(GameSettings.SCREEN_SHAKE_STRENGTH, GameSettings.SCREEN_SHAKE_DURATION_MS);
                }
                bullet.deactivate();
            }
        }
    }
    
    private void triggerExplosion(ExplosiveBarrel barrel, long now) {
        barrel.active = false;
        SoundManager.getInstance().playEffect("explosion"); 
        triggerCameraShake(GameSettings.SCREEN_SHAKE_STRENGTH * 3.0, 350L);

        double centerX = barrel.x + 16.0;
        double centerY = barrel.y + 21.0;

        
        addEffect("explosion", explosionImage, centerX - 90, centerY - 90, 180, 180, 300L);

        
        applyExplosionForce(p1, centerX, centerY, now);
        applyExplosionForce(p2, centerX, centerY, now);
    }
    
    private void applyExplosionForce(Player player, double ex, double ey, long now) {
        if (player.isInvulnerable(now)) return;

        var b = player.getBounds();
        double px = b.getMinX() + b.getWidth() * 0.5;
        double py = b.getMinY() + b.getHeight() * 0.5;

        double dx = px - ex;
        double dy = py - ey;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double radius = 170.0; 

        if (dist < radius) {
            double forceMultiplier = 1.0 - (dist / radius);
            
            double forceX = (dx / dist) * 1250.0 * forceMultiplier;
            double forceY = -750.0 * forceMultiplier - 150.0; 

            player.applyKnockback(forceX, forceY);
            addEffect("blood", bloodImage, px - 15, py - 15, 30, 30, 250L);
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

    private void handleBlastZoneDeaths(long now) {
        if (gameOver) {
            return;
        }

        boolean p1Out = isOutOfMap(p1);
        boolean p2Out = isOutOfMap(p2);
        if (!p1Out && !p2Out) {
            return;
        }

        SoundManager.getInstance().playEffect("die");

        bullets.clear();
        weaponDrops.clear();
        barrels.clear();

        if (p1Out && p2Out) {
            p1Stocks--;
            p2Stocks--;
            showCenterBanner("DOUBLE KO", 900L);
            freezeUntilMillis = Math.max(freezeUntilMillis, now + 900L);
            triggerCameraShake(GameSettings.SCREEN_SHAKE_STRENGTH * 1.2, 150L);

            if (p1Stocks <= 0 || p2Stocks <= 0) {
                if (p1Stocks == p2Stocks) {
                    queueNextRound(now, "ROUND DRAW");
                } else if (p1Stocks < p2Stocks) {
                    concludeRound("Player 2", now);
                } else {
                    concludeRound("Player 1", now);
                }
                return;
            }

            respawnPlayerFromSky(p1, now);
            respawnPlayerFromSky(p2, now);
            return;
        }

        Player loser = p1Out ? p1 : p2;
        String loserName = p1Out ? "P1" : "P2";
        String winner = p1Out ? "Player 2" : "Player 1";

        if (p1Out) {
            p1Stocks--;
        } else {
            p2Stocks--;
        }

        showCenterBanner(loserName + " KO", 700L);
        freezeUntilMillis = Math.max(freezeUntilMillis, now + 700L);
        triggerCameraShake(GameSettings.SCREEN_SHAKE_STRENGTH * 0.9, 120L);

        if ((loser == p1 && p1Stocks <= 0) || (loser == p2 && p2Stocks <= 0)) {
            concludeRound(winner, now);
            return;
        }

        respawnPlayerFromSky(loser, now);
    }

    private void concludeRound(String winner, long now) {
        if ("Player 1".equals(winner)) {
            p1RoundWins++;
        } else {
            p2RoundWins++;
        }

        if (p1RoundWins >= GameSettings.ROUND_WINS_TO_MATCH || p2RoundWins >= GameSettings.ROUND_WINS_TO_MATCH) {
            finishGame(winner);
            return;
        }

        queueNextRound(now, winner + " TAKES ROUND");
    }

    private void queueNextRound(long now, String bannerText) {
        showCenterBanner(bannerText, GameSettings.ROUND_END_DELAY_MS);
        pendingNextRoundAtMillis = now + GameSettings.ROUND_END_DELAY_MS;
        freezeUntilMillis = pendingNextRoundAtMillis;
    }

    private boolean isOutOfMap(Player player) {
        var b = player.getBounds();
        double margin = GameSettings.BLAST_ZONE_MARGIN;
        return b.getMinY() > GameSettings.HEIGHT + margin
                || b.getMaxY() < -margin
                || b.getMaxX() < -margin
                || b.getMinX() > GameSettings.WIDTH + margin;
    }

    private void renderGame() {
        long now = System.currentTimeMillis();

        gc.setFill(Color.web("#d9ecff"));
        gc.fillRect(0, 0, GameSettings.WIDTH, GameSettings.HEIGHT);

        double shakeX = 0.0;
        double shakeY = 0.0;
        if (now < shakeUntilMillis) {
            double t = (shakeUntilMillis - now) / (double) Math.max(1L, GameSettings.SCREEN_SHAKE_DURATION_MS);
            double strength = Math.max(0.2, t) * shakeStrength;
            shakeX = (random.nextDouble() - 0.5) * 2.0 * strength;
            shakeY = (random.nextDouble() - 0.5) * 2.0 * strength;
        }

        gc.save();
        gc.translate((-cameraX + shakeX) * cameraZoom, (-cameraY + shakeY) * cameraZoom);
        gc.scale(cameraZoom, cameraZoom);
        renderExtendedMap();
        renderPlatformGuides();

        List<Renderable> renderables = new ArrayList<>();
        renderables.add(p1);
        renderables.add(p2);
        renderables.addAll(bullets);
        for (Renderable renderable : renderables) {
            renderable.render(gc);
        }

        renderWeaponDrops();
        renderBarrels();

        for (HitEffect effect : hitEffects) {
            if (effect.image != EMPTY_IMAGE) {
                gc.drawImage(effect.image, effect.x, effect.y, effect.width, effect.height);
            } else if ("explosion".equals(effect.type)) {
                
                double t = Math.max(0, (double) (effect.expiresAt - now) / effect.totalLife);
                gc.setFill(Color.web("#ff4400", t * 0.75));
                gc.fillOval(effect.x, effect.y, effect.width, effect.height);
                gc.setFill(Color.web("#ffcc00", t * 0.95));
                gc.fillOval(effect.x + effect.width * 0.25, effect.y + effect.height * 0.25, effect.width * 0.5, effect.height * 0.5);
            }
        }
        gc.restore();

        renderBlastZoneWarning();

        gc.setStroke(Color.web("#2f3541"));
        gc.strokeRect(1, 1, GameSettings.WIDTH - 2, GameSettings.HEIGHT - 2);

        renderHud();
        renderCenterBanner(now);
    }
    
    private void renderBarrels() {
        for (ExplosiveBarrel barrel : barrels) {
            if (!barrel.active) continue;
            
            if (barrelImage != EMPTY_IMAGE) {
                gc.drawImage(barrelImage, barrel.x, barrel.y, 32, 42);
            } else {
                
                gc.setFill(Color.web("#a32222"));
                gc.fillRoundRect(barrel.x, barrel.y, 32, 42, 6, 6);
                gc.setFill(Color.web("#333333"));
                gc.fillRect(barrel.x, barrel.y + 8, 32, 4);
                gc.fillRect(barrel.x, barrel.y + 30, 32, 4);
                gc.setFill(Color.YELLOW);
                gc.setFont(Font.font("Impact", FontWeight.NORMAL, 14));
                gc.fillText("TNT", barrel.x + 5, barrel.y + 25);
            }
        }
    }

    private void renderExtendedMap() {
        double m = MAP_RENDER_EXTEND_MARGIN;
        gc.drawImage(
                selectedMapImage,
                -m,
                -m,
                GameSettings.WIDTH + (m * 2.0),
                GameSettings.HEIGHT + (m * 2.0)
        );
    }

    private void renderBlastZoneWarning() {
        gc.setFill(Color.web("#ff3344", 0.18));
        gc.fillRect(0, 0, GameSettings.WIDTH, 8);
        gc.fillRect(0, GameSettings.HEIGHT - 8, GameSettings.WIDTH, 8);
        gc.fillRect(0, 0, 8, GameSettings.HEIGHT);
        gc.fillRect(GameSettings.WIDTH - 8, 0, 8, GameSettings.HEIGHT);
    }

    private void updateCamera(double deltaSeconds) {
        var b1 = p1.getBounds();
        var b2 = p2.getBounds();

        double minX = Math.min(b1.getMinX(), b2.getMinX());
        double maxX = Math.max(b1.getMaxX(), b2.getMaxX());
        double minY = Math.min(b1.getMinY(), b2.getMinY());
        double maxY = Math.max(b1.getMaxY(), b2.getMaxY());

        double focusWidth = Math.max(1.0, (maxX - minX) + CAMERA_MARGIN_X);
        double focusHeight = Math.max(1.0, (maxY - minY) + CAMERA_MARGIN_Y);
        double zoomByWidth = GameSettings.WIDTH / focusWidth;
        double zoomByHeight = GameSettings.HEIGHT / focusHeight;
        double targetZoom = clamp(Math.min(zoomByWidth, zoomByHeight), CAMERA_MIN_ZOOM, CAMERA_MAX_ZOOM);

        double centerX = (minX + maxX) * 0.5;
        double centerY = (minY + maxY) * 0.5;
        double viewWidth = GameSettings.WIDTH / targetZoom;
        double viewHeight = GameSettings.HEIGHT / targetZoom;
        double targetCameraX = clamp(
                centerX - (viewWidth * 0.5),
                -GameSettings.BLAST_ZONE_MARGIN,
                GameSettings.WIDTH - viewWidth + GameSettings.BLAST_ZONE_MARGIN
        );
        double targetCameraY = clamp(
                centerY - (viewHeight * 0.5),
                -GameSettings.BLAST_ZONE_MARGIN,
                GameSettings.HEIGHT - viewHeight + GameSettings.BLAST_ZONE_MARGIN
        );

        double t = clamp(deltaSeconds * CAMERA_LERP_SPEED, 0.0, 1.0);
        cameraZoom += (targetZoom - cameraZoom) * t;
        cameraX += (targetCameraX - cameraX) * t;
        cameraY += (targetCameraY - cameraY) * t;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void renderPlatformGuides() {
        gc.setFill(Color.web("#2f7cff", 0.45));
        gc.setStroke(Color.web("#0f3fa1", 0.85));
        gc.setLineWidth(1.8);
        for (PlatformSurface surface : worldSurfaces) {
            var b = surface.getBounds();
            gc.fillRoundRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight(), 8, 8);
            gc.strokeRoundRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight(), 8, 8);
        }
    }

    private void renderHud() {
        long now = System.currentTimeMillis();
        double panelY = 10;
        double panelHeight = 82;
        double sideWidth = 318;
        double centerWidth = 220;

        drawHudFrame(14, panelY, sideWidth, panelHeight, Color.web("#14253a", 0.82), Color.web("#7fd1ff", 0.95));
        gc.setFill(Color.web("#7fd1ff"));
        gc.setFont(Font.font("Impact", FontWeight.NORMAL, 20));
        gc.fillText("P1", 28, 34);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        gc.fillText("Weapon: " + p1.getEquippedGun(now).label(), 28, 56);
        gc.fillText("Rounds: " + p1RoundWins + " / " + GameSettings.ROUND_WINS_TO_MATCH, 28, 74);
        drawStocks(200, 63, p1Stocks, Color.web("#6ed4ff"), true);

        double rightX = GameSettings.WIDTH - sideWidth - 14;
        drawHudFrame(rightX, panelY, sideWidth, panelHeight, Color.web("#3a1717", 0.82), Color.web("#ff8a9b", 0.95));
        gc.setFill(Color.web("#ff8a9b"));
        gc.setFont(Font.font("Impact", FontWeight.NORMAL, 20));
        gc.fillText("P2", rightX + 16, 34);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        gc.fillText("Weapon: " + p2.getEquippedGun(now).label(), rightX + 16, 56);
        gc.fillText("Rounds: " + p2RoundWins + " / " + GameSettings.ROUND_WINS_TO_MATCH, rightX + 16, 74);
        drawStocks(rightX + sideWidth - 22, 63, p2Stocks, Color.web("#ff8096"), false);

        double centerX = (GameSettings.WIDTH - centerWidth) * 0.5;
        drawHudFrame(centerX, panelY, centerWidth, panelHeight, Color.web("#1b1f2a", 0.82), Color.web("#ffe175", 0.95));
        gc.setFill(Color.web("#ffe175"));
        gc.setFont(Font.font("Impact", FontWeight.NORMAL, 20));
        gc.fillText("ROUND " + roundNumber, centerX + 18, 34);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        long secondsToDrop = Math.max(0L, (nextGunDropAtMillis - now + 999) / 1000);
        gc.fillText("Next Drop: " + secondsToDrop + "s", centerX + 18, 56);
        gc.fillText("Stock: " + GameSettings.STOCKS_PER_ROUND, centerX + 18, 74);

        drawHudFrame(140, GameSettings.HEIGHT - 36, GameSettings.WIDTH - 280, 24, Color.web("#101319", 0.74), Color.web("#95a9c2", 0.55));
        gc.setFill(Color.web("#d6e7ff"));
        gc.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        gc.fillText(
                "P1: A/D Move, W Jump, S Drop, SPACE Attack   |   P2: Arrows Move, UP Jump, DOWN Drop, ENTER Attack   |   ESC Pause",
                155,
                GameSettings.HEIGHT - 19
        );
    }

    private void drawHudFrame(double x, double y, double width, double height, Color fill, Color stroke) {
        gc.setFill(fill);
        gc.fillRoundRect(x, y, width, height, 14, 14);
        gc.setStroke(stroke);
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x, y, width, height, 14, 14);
    }

    private void drawStocks(double startX, double y, int stocks, Color aliveColor, boolean fromLeft) {
        for (int i = 0; i < GameSettings.STOCKS_PER_ROUND; i++) {
            double x = fromLeft ? (startX + i * 17.0) : (startX - i * 17.0);
            gc.setFill(i < stocks ? aliveColor : Color.web("#545d68"));
            gc.fillOval(x, y, 11, 11);
            gc.setStroke(Color.web("#101820"));
            gc.setLineWidth(1.1);
            gc.strokeOval(x, y, 11, 11);
        }
    }

    private void renderCenterBanner(long now) {
        if (centerBannerText == null || centerBannerText.isBlank() || now > centerBannerUntilMillis) {
            return;
        }

        double width = 410;
        double height = 84;
        double x = (GameSettings.WIDTH - width) * 0.5;
        double y = (GameSettings.HEIGHT - height) * 0.5 - 24;

        gc.setFill(Color.web("#000000", 0.74));
        gc.fillRoundRect(x, y, width, height, 18, 18);
        gc.setStroke(Color.web("#ffe175", 0.95));
        gc.setLineWidth(2.8);
        gc.strokeRoundRect(x, y, width, height, 18, 18);

        gc.setFill(Color.web("#fff4af"));
        gc.setFont(Font.font("Impact", FontWeight.NORMAL, 42));
        gc.fillText(centerBannerText, x + 36, y + 54);
    }

    private void showCenterBanner(String text, long durationMillis) {
        centerBannerText = text;
        centerBannerUntilMillis = System.currentTimeMillis() + durationMillis;
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

        Label title = new Label(winner + " Wins Match!");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Impact", FontWeight.NORMAL, 40));

        Label score = new Label("Score " + p1RoundWins + " - " + p2RoundWins);
        score.setTextFill(Color.web("#d0e5ff"));
        score.setFont(Font.font("Consolas", FontWeight.BOLD, 20));

        Button restart = new Button("Play Again");
        restart.setPrefWidth(220);
        styleMenuButton(restart, "#3c8cff", "#1f5ec9");
        restart.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            shutdownGameSystems();
            onRematch.run();
        });

        Button backToMenu = new Button("Return to Menu");
        backToMenu.setPrefWidth(220);
        styleMenuButton(backToMenu, "#3a4354", "#252d39");
        backToMenu.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            shutdownGameSystems();
            onBackToMenu.run();
        });

        VBox modal = new VBox(12, title, score, restart, backToMenu);
        modal.setAlignment(Pos.CENTER);
        modal.setMaxWidth(420);
        modal.setMaxHeight(280);
        modal.setStyle("-fx-background-color: rgba(0,0,0,0.83); -fx-padding: 24; -fx-background-radius: 14;");
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

    private void respawnPlayerFromSky(Player player, long nowMillis) {
        double minX = SIDE_RESPAWN_MARGIN;
        double maxX = GameSettings.WIDTH - GameSettings.PLAYER_WIDTH - SIDE_RESPAWN_MARGIN;
        double spawnX = minX + random.nextDouble() * Math.max(1.0, maxX - minX);
        double spawnY = -(SKY_RESPAWN_Y_MIN + random.nextDouble() * (SKY_RESPAWN_Y_MAX - SKY_RESPAWN_Y_MIN));
        player.respawnFromSky(spawnX, spawnY, nowMillis);
    }

    private void spawnRoundPlayers(long nowMillis) {
        p1.respawnFromSky(sx(122), sy(98), nowMillis);
        p2.respawnFromSky(sx(438), sy(98), nowMillis);
    }

    private void prepareRound(int targetRoundNumber) {
        long now = System.currentTimeMillis();
        roundNumber = targetRoundNumber;
        p1Stocks = GameSettings.STOCKS_PER_ROUND;
        p2Stocks = GameSettings.STOCKS_PER_ROUND;
        bullets.clear();
        weaponDrops.clear();
        hitEffects.clear();
        
        barrels.clear();
        spawnBarrels(); 
        
        spawnRoundPlayers(now);
        freezeUntilMillis = now + GameSettings.ROUND_START_COUNTDOWN_MS;
        nextGunDropAtMillis = freezeUntilMillis + GameSettings.FIRST_DROP_DELAY_MS;
        nextBarrelDropAtMillis = freezeUntilMillis + GameSettings.BARREL_DROP_INTERVAL_MS;
        showCenterBanner("ROUND " + roundNumber, GameSettings.ROUND_START_COUNTDOWN_MS);
    }
    
    private void spawnBarrels() {
        if (worldSurfaces.isEmpty()) return;

        
        int numBarrels = random.nextInt(3) + 1;
        
        
        List<PlatformSurface> candidates = worldSurfaces.stream()
                .filter(s -> s.getBounds().getWidth() > 70.0)
                .toList();

        if (candidates.isEmpty()) return;

        for (int i = 0; i < numBarrels; i++) {
            PlatformSurface surface = candidates.get(random.nextInt(candidates.size()));
            var bounds = surface.getBounds();
            double spawnMinX = bounds.getMinX() + 6.0;
            double spawnMaxX = bounds.getMaxX() - 32.0 - 6.0;
            
            if (spawnMaxX > spawnMinX) {
                double spawnX = spawnMinX + random.nextDouble() * (spawnMaxX - spawnMinX);
                double spawnY = bounds.getMinY() - 42.0; 
                barrels.add(new ExplosiveBarrel(spawnX, spawnY));
            }
        }
    }

    private void updateBarrelDrops(long now) {
        if (now < nextBarrelDropAtMillis) {
            return;
        }
        
        
        if (barrels.size() >= 3) {
            nextBarrelDropAtMillis = now + GameSettings.BARREL_DROP_INTERVAL_MS;
            return;
        }

        if (worldSurfaces.isEmpty()) return;

        List<PlatformSurface> candidates = worldSurfaces.stream()
                .filter(s -> s.getBounds().getWidth() > 70.0)
                .toList();

        if (!candidates.isEmpty()) {
            PlatformSurface surface = candidates.get(random.nextInt(candidates.size()));
            var bounds = surface.getBounds();
            double spawnMinX = bounds.getMinX() + 6.0;
            double spawnMaxX = bounds.getMaxX() - 32.0 - 6.0;
            
            if (spawnMaxX > spawnMinX) {
                double spawnX = spawnMinX + random.nextDouble() * (spawnMaxX - spawnMinX);
                double spawnY = bounds.getMinY() - 42.0;
                barrels.add(new ExplosiveBarrel(spawnX, spawnY));
            }
        }
        
        nextBarrelDropAtMillis = now + GameSettings.BARREL_DROP_INTERVAL_MS;
    }

    private void setupPauseUi() {
        pauseButton.setFocusTraversable(false);
        pauseButton.setPrefWidth(100);
        styleMenuButton(pauseButton, "#4c5f80", "#2f3b53");
        pauseButton.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            togglePause();
        });
        StackPane.setAlignment(pauseButton, Pos.TOP_RIGHT);
        StackPane.setMargin(pauseButton, new Insets(12, 12, 0, 0));

        Label title = new Label("Paused");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Impact", FontWeight.NORMAL, 38));

        Button cont = new Button("Continue");
        cont.setPrefWidth(220);
        styleMenuButton(cont, "#3c8cff", "#1f5ec9");
        cont.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            setPaused(false);
        });

        Button restart = new Button("Restart Match");
        restart.setPrefWidth(220);
        styleMenuButton(restart, "#70839a", "#4d5f74");
        restart.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            shutdownGameSystems();
            onRematch.run();
        });

        Button menu = new Button("Return to Menu");
        menu.setPrefWidth(220);
        styleMenuButton(menu, "#3a4354", "#252d39");
        menu.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            shutdownGameSystems();
            onBackToMenu.run();
        });

        pauseModal = new VBox(14, title, cont, restart, menu);
        pauseModal.setAlignment(Pos.CENTER);
        pauseModal.setVisible(false);
        pauseModal.setMaxWidth(360);
        pauseModal.setMaxHeight(280);
        pauseModal.setStyle("-fx-background-color: rgba(0,0,0,0.84); -fx-padding: 24; -fx-background-radius: 14;");

        getChildren().addAll(pauseModal, pauseButton);
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
        } else {
            lastFrameNanos = 0L;
        }
        if (pauseModal != null) {
            pauseModal.setVisible(paused);
        }
    }

    private void shutdownGameSystems() {
        gameLoop.stop();
        SoundManager.getInstance().stopBgm(); 
    }

    private List<PlatformSurface> createSurfacesForMap(String mapResourcePath) {
        if ("/Map2.png".equals(mapResourcePath)) {
            return List.of(
                    new PlatformSurface(sx(67), sy(72), sw(132), sh(12), true),
                    new PlatformSurface(sx(55), sy(112), sw(228), sh(12), true),
                    new PlatformSurface(sx(24), sy(160), sw(380), sh(13), true),
                    new PlatformSurface(sx(0), sy(210), sw(540), sh(13), true),
                    new PlatformSurface(sx(0), sy(264), sw(598), sh(14), true)
            );
        }

        if ("/Map3.png".equals(mapResourcePath)) {
            return List.of(
                    new PlatformSurface(sx(78), sy(118), sw(66), sh(10), true),
                    new PlatformSurface(sx(454), sy(118), sw(66), sh(10), true),
                    new PlatformSurface(sx(18), sy(163), sw(120), sh(12), true),
                    new PlatformSurface(sx(462), sy(163), sw(120), sh(12), true),
                    new PlatformSurface(sx(198), sy(198), sw(198), sh(12), true),
                    new PlatformSurface(sx(0), sy(286), sw(598), sh(16), true)
            );
        }

        return List.of(
                new PlatformSurface(sx(106), sy(146), sw(148), sh(12), true),
                new PlatformSurface(sx(309), sy(146), sw(145), sh(12), true),
                new PlatformSurface(sx(62), sy(214), sw(493), sh(14), true),
                new PlatformSurface(sx(107), sy(285), sw(88), sh(12), true),
                new PlatformSurface(sx(400), sy(285), sw(89), sh(12), true),
                new PlatformSurface(sx(85), sy(327), sw(434), sh(14), true)
        );
    }

    private static Image loadImage(String resourcePath) {
        return loadImageInternal(resourcePath, false);
    }

    private static Image loadTransparentImage(String resourcePath) {
        return loadImageInternal(resourcePath, true);
    }

    private static Image loadImageInternal(String resourcePath, boolean transparent) {
        Image image;
        var url = GamePanel.class.getResource(resourcePath);
        if (url != null) {
            image = new Image(url.toExternalForm(), false);
            if (image.isError()) {
                return EMPTY_IMAGE;
            }
            return transparent ? makeBackgroundTransparent(image) : image;
        }

        Path fallback = Paths.get("src", "main", "resources", resourcePath.replaceFirst("^/", ""));
        if (Files.exists(fallback)) {
            image = new Image(fallback.toUri().toString(), false);
            if (image.isError()) {
                return EMPTY_IMAGE;
            }
            return transparent ? makeBackgroundTransparent(image) : image;
        }
        return EMPTY_IMAGE; 
    }

    private static Image makeBackgroundTransparent(Image raw) {
        if (raw == null || raw.isError() || raw == EMPTY_IMAGE) {
            return EMPTY_IMAGE;
        }
        int w = (int) Math.round(raw.getWidth());
        int h = (int) Math.round(raw.getHeight());
        if (w <= 0 || h <= 0) {
            return EMPTY_IMAGE;
        }

        PixelReader reader = raw.getPixelReader();
        if (reader == null) {
            return EMPTY_IMAGE;
        }

        WritableImage out = new WritableImage(w, h);
        PixelWriter writer = out.getPixelWriter();
        Color key = reader.getColor(0, 0);

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                Color c = reader.getColor(px, py);
                boolean transparentByKey = colorDistance(c, key) < 0.18;
                boolean transparentByWhite = c.getOpacity() > 0.0
                        && c.getBrightness() > 0.94
                        && c.getSaturation() < 0.16;
                if (transparentByKey || transparentByWhite) {
                    writer.setColor(px, py, Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.0));
                } else {
                    writer.setColor(px, py, c);
                }
            }
        }
        return out;
    }

    private static double colorDistance(Color a, Color b) {
        double dr = a.getRed() - b.getRed();
        double dg = a.getGreen() - b.getGreen();
        double db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private void addEffect(String type, Image image, double x, double y, double width, double height, long lifeMillis) {
        hitEffects.add(new HitEffect(type, image, x, y, width, height, System.currentTimeMillis() + lifeMillis, lifeMillis));
    }

    private void removeExpiredEffects(long nowMillis) {
        hitEffects.removeIf(effect -> effect.expiresAt <= nowMillis);
    }

    private void triggerCameraShake(double strength, long durationMillis) {
        shakeStrength = Math.max(shakeStrength, strength);
        shakeUntilMillis = Math.max(shakeUntilMillis, System.currentTimeMillis() + durationMillis);
    }

    private void updateWeaponDrops(long now) {
        if (now < nextGunDropAtMillis) {
            return;
        }
        if (weaponDrops.size() >= 2) {
            nextGunDropAtMillis = now + GameSettings.NEXT_DROP_INTERVAL_MS;
            return;
        }

        WeaponDrop drop = createWeaponDrop();
        if (drop != null) {
            weaponDrops.add(drop);
        }
        nextGunDropAtMillis = now + GameSettings.NEXT_DROP_INTERVAL_MS;
    }

    private WeaponDrop createWeaponDrop() {
        if (worldSurfaces.isEmpty()) {
            return null;
        }

        List<PlatformSurface> candidates = worldSurfaces.stream()
                .filter(surface -> surface.getBounds().getWidth() > GameSettings.BOX_SIZE + 20.0)
                .sorted(Comparator.comparingDouble(surface -> surface.getBounds().getMinY()))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }

        PlatformSurface selectedSurface = candidates.get(random.nextInt(candidates.size()));
        var bounds = selectedSurface.getBounds();
        double spawnMinX = bounds.getMinX() + 6.0;
        double spawnMaxX = bounds.getMaxX() - GameSettings.BOX_SIZE - 6.0;
        double spawnX = spawnMinX + random.nextDouble() * Math.max(1.0, spawnMaxX - spawnMinX);
        double spawnY = bounds.getMinY() - GameSettings.BOX_SIZE - 2.0;

        List<Gun> randomPool = GunRegistry.SELECTABLE_GUNS;
        Gun gun = randomPool.get(random.nextInt(randomPool.size()));
        return new WeaponDrop(spawnX, spawnY, gun);
    }

    private void handleWeaponPickups(long nowMillis) {
        if (weaponDrops.isEmpty()) {
            return;
        }
        weaponDrops.removeIf(drop -> tryPickup(drop, p1, nowMillis) || tryPickup(drop, p2, nowMillis));
    }

    private boolean tryPickup(WeaponDrop drop, Player player, long nowMillis) {
        if (!drop.bounds().intersects(player.getBounds())) {
            return false;
        }
        player.equipGun(drop.gun(), nowMillis);
        SoundManager.getInstance().playEffect("pickup"); 
        addEffect("pickup", bulletHitImage, drop.x() + 4, drop.y() + 4, 24, 24, 160L);
        return true;
    }

    private void renderWeaponDrops() {
        for (WeaponDrop drop : weaponDrops) {
            gc.setFill(Color.web("#ffe69a"));
            gc.fillRoundRect(drop.x(), drop.y(), GameSettings.BOX_SIZE, GameSettings.BOX_SIZE, 8, 8);
            gc.setStroke(Color.web("#8f5f00"));
            gc.setLineWidth(2.0);
            gc.strokeRoundRect(drop.x(), drop.y(), GameSettings.BOX_SIZE, GameSettings.BOX_SIZE, 8, 8);
            gc.drawImage(drop.gun().sprite(), drop.x() + 4, drop.y() + 9, GameSettings.BOX_SIZE - 8, 14);
        }
    }

    private record WeaponDrop(double x, double y, Gun gun) {
        private javafx.geometry.Rectangle2D bounds() {
            return new javafx.geometry.Rectangle2D(x, y, GameSettings.BOX_SIZE, GameSettings.BOX_SIZE);
        }
    }

    
    private static final class ExplosiveBarrel {
        double x, y;
        boolean active = true;

        ExplosiveBarrel(double x, double y) {
            this.x = x;
            this.y = y;
        }

        Rectangle2D getBounds() {
            return new Rectangle2D(x, y, 32, 42); 
        }
    }

    private static final class HitEffect {
        private final String type;
        private final Image image;
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final long expiresAt;
        private final long totalLife; 

        private HitEffect(String type, Image image, double x, double y, double width, double height, long expiresAt, long totalLife) {
            this.type = type;
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.expiresAt = expiresAt;
            this.totalLife = totalLife;
        }
    }
}