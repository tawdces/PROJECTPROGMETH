package game.ui;

import game.config.GameSettings;
import game.entities.Bullet;
import game.entities.Player;
import game.entities.powerups.PowerUp;
import game.entities.traps.Landmine;
import game.entities.traps.Trap;
import game.entities.weapons.Gun;
import game.logic.Renderable;
import game.logic.SharedMultiplayerCamera;
import game.logic.SoundManager;
import game.logic.Updatable;
import game.map.GameMap;
import game.map.PlatformSurface;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
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
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GamePanel extends StackPane {
    private static final double SKY_RESPAWN_Y_MIN = 260.0;
    private static final double SKY_RESPAWN_Y_MAX = 430.0;
    private static final double SIDE_RESPAWN_MARGIN = 48.0;
    private static final double RESPAWN_CENTER_SPREAD = 140.0;
    private static final double RESPAWN_PAIR_OFFSET = 88.0;
    private static final double RESPAWN_PAIR_JITTER = 24.0;
    private static final double MAP_RENDER_EXTEND_MARGIN = GameSettings.BLAST_ZONE_MARGIN + 24.0;
    private static final double DOUBLE_JUMP_EFFECT_SIZE = 44.0;
    private static final long DOUBLE_JUMP_EFFECT_LIFE_MS = 240L;
    private static final double DOUBLE_JUMP_EFFECT_START_SCALE = 0.82;
    private static final double DOUBLE_JUMP_EFFECT_END_SCALE = 1.48;
    private static final double DOUBLE_JUMP_EFFECT_MAX_ALPHA = 0.95;
    private static final Image EMPTY_IMAGE = GameImageLoader.emptyImage();
    private static final boolean SHOW_PLATFORM_GUIDES = false;

    private final Runnable onRematch;
    private final Runnable onBackToMenu;

    private final Canvas canvas = new Canvas(GameSettings.WIDTH, GameSettings.HEIGHT);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final GameMap selectedMap;
    private final Image bulletHitImage = GameImageLoader.loadTransparentImage(GamePanel.class, "/images/effects/Bullet_hit.png");
    private final Image bloodImage = GameImageLoader.loadTransparentImage(GamePanel.class, "/images/effects/Blood.png");
    private final Image explosionImage = GameImageLoader.loadTransparentImage(GamePanel.class, "/images/effects/Explosion.png");

    private final Player p1;
    private final Player p2;

    private final List<Bullet> bullets = new ArrayList<>();
    private final MatchDropCoordinator dropCoordinator;
    private final List<WeaponDrop> weaponDrops;
    private final List<Trap> traps;
    private final List<PowerUp> powerUps;
    private final List<GameEffect> hitEffects = new ArrayList<>();
    private final List<PlatformSurface> worldSurfaces;
    private final SharedMultiplayerCamera sharedCamera;
    private final List<Player> trackedPlayers = new ArrayList<>(4);
    private final List<Rectangle2D> trackedPlayerBounds = new ArrayList<>(4);
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private long p1JumpBufferedUntilMillis;
    private long p2JumpBufferedUntilMillis;
    private long p1DropBufferedUntilMillis;
    private long p2DropBufferedUntilMillis;
    private final Random random = new Random();
    private final PauseOverlay pauseOverlay;

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
    private double cameraZoom = GameSettings.CAMERA_MIN_ZOOM;

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

    public GamePanel(GameMap selectedMap, Gun p1Weapon, Gun p2Weapon, Runnable onRematch, Runnable onBackToMenu) {
        this.onRematch = onRematch;
        this.onBackToMenu = onBackToMenu;
        this.selectedMap = selectedMap == null ? GameMap.defaultMap() : selectedMap;
        this.worldSurfaces = this.selectedMap.surfaces();
        this.dropCoordinator = new MatchDropCoordinator(this.worldSurfaces, random);
        this.weaponDrops = dropCoordinator.weaponDrops();
        this.traps = dropCoordinator.traps();
        this.powerUps = dropCoordinator.powerUps();

        p1 = new Player(
                this.selectedMap.playerOneSpawnX(),
                this.selectedMap.spawnGroundY() - GameSettings.PLAYER_HEIGHT,
                "P1",
                Color.DODGERBLUE,
                1,
                "/images/players/Player1.png",
                List.of()
        );
        p2 = new Player(
                this.selectedMap.playerTwoSpawnX(),
                this.selectedMap.spawnGroundY() - GameSettings.PLAYER_HEIGHT,
                "P2",
                Color.CRIMSON,
                -1,
                "/images/players/Player2.png",
                List.of()
        );
        p1.equipPermanentGun(p1Weapon);
        p2.equipPermanentGun(p2Weapon);
        registerTrackedPlayer(p1);
        registerTrackedPlayer(p2);

        sharedCamera = new SharedMultiplayerCamera(
                GameSettings.WIDTH,
                GameSettings.HEIGHT,
                -GameSettings.BLAST_ZONE_MARGIN,
                GameSettings.WIDTH + GameSettings.BLAST_ZONE_MARGIN,
                -GameSettings.BLAST_ZONE_MARGIN,
                GameSettings.HEIGHT + GameSettings.BLAST_ZONE_MARGIN,
                GameSettings.CAMERA_MIN_ZOOM,
                GameSettings.CAMERA_MAX_ZOOM,
                GameSettings.CAMERA_DYNAMIC_ZOOM,
                GameSettings.CAMERA_FIXED_ZOOM,
                GameSettings.CAMERA_PADDING_X,
                GameSettings.CAMERA_PADDING_Y,
                GameSettings.CAMERA_FOLLOW_SPEED
        );

        setPrefSize(GameSettings.WIDTH, GameSettings.HEIGHT);
        getChildren().add(canvas);
        pauseOverlay = new PauseOverlay(
                this,
                this::togglePause,
                () -> setPaused(false),
                this::restartMatch,
                this::backToMenu
        );
        prepareRound(1);
        updateCamera(0.0);

        SoundManager.getInstance().playRandomBgm();

        gameLoop.start();
    }

    public void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            boolean isFreshPress = pressedKeys.add(event.getCode());
            if (!isFreshPress) {
                return;
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                SoundManager.getInstance().playEffect("click");
                togglePause();
                return;
            }

            long now = System.currentTimeMillis();
            if (paused || gameOver) {
                return;
            }

            if (event.getCode() == KeyCode.W) {
                p1JumpBufferedUntilMillis = now + GameSettings.JUMP_INPUT_BUFFER_MS;
            } else if (event.getCode() == KeyCode.UP) {
                p2JumpBufferedUntilMillis = now + GameSettings.JUMP_INPUT_BUFFER_MS;
            } else if (event.getCode() == KeyCode.S) {
                p1DropBufferedUntilMillis = now + GameSettings.DROP_INPUT_BUFFER_MS;
            } else if (event.getCode() == KeyCode.DOWN) {
                p2DropBufferedUntilMillis = now + GameSettings.DROP_INPUT_BUFFER_MS;
            }
        });

        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }

    private void registerTrackedPlayer(Player player) {
        if (player == null || trackedPlayers.size() >= 4) {
            return;
        }
        trackedPlayers.add(player);
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

        boolean combatLocked = isCombatLocked(now);
        processBufferedVerticalInputs(now);
        updateMovement();
        if (!combatLocked) {
            updateActions(now);
        }

        List<Updatable> updatables = new ArrayList<>();
        updatables.add(p1);
        updatables.add(p2);
        updatables.addAll(bullets);
        updatables.addAll(traps);
        updatables.addAll(powerUps);
        for (Updatable updatable : updatables) {
            updatable.update(deltaSeconds);
        }

        p1.resolveCollisions(worldSurfaces, now);
        p2.resolveCollisions(worldSurfaces, now);
        removeExpiredEffects(now);

        bullets.removeIf(bullet -> !bullet.isActive());
        traps.removeIf(trap -> !trap.isActive());
        powerUps.removeIf(p -> !p.isActive());

        if (!combatLocked) {
            checkLandmineTriggers(now);
            handleBulletHits(now);
            dropCoordinator.updateDrops(now);
            handleWeaponPickups(now);
            handlePowerUpPickups(now);
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

    private void updateActions(long nowMillis) {
        if (p1.canAction(nowMillis) && pressedKeys.contains(KeyCode.SPACE)) {
            performAction(p1, p2);
        }
        if (p2.canAction(nowMillis) && pressedKeys.contains(KeyCode.ENTER)) {
            performAction(p2, p1);
        }
    }

    private void processBufferedVerticalInputs(long nowMillis) {
        if (p1JumpBufferedUntilMillis > 0L && nowMillis <= p1JumpBufferedUntilMillis) {
            Player.JumpResult jumpResult = p1.jumpWithResult(nowMillis);
            if (jumpResult != Player.JumpResult.NONE) {
                p1JumpBufferedUntilMillis = 0L;
                if (jumpResult == Player.JumpResult.AIR) {
                    spawnDoubleJumpEffect(p1);
                }
            }
        }
        if (p2JumpBufferedUntilMillis > 0L && nowMillis <= p2JumpBufferedUntilMillis) {
            Player.JumpResult jumpResult = p2.jumpWithResult(nowMillis);
            if (jumpResult != Player.JumpResult.NONE) {
                p2JumpBufferedUntilMillis = 0L;
                if (jumpResult == Player.JumpResult.AIR) {
                    spawnDoubleJumpEffect(p2);
                }
            }
        }
        if (p1DropBufferedUntilMillis > 0L && nowMillis <= p1DropBufferedUntilMillis && canDropToLowerPlatform(p1)) {
            p1.requestDropThrough(nowMillis);
            p1DropBufferedUntilMillis = 0L;
        }
        if (p2DropBufferedUntilMillis > 0L && nowMillis <= p2DropBufferedUntilMillis && canDropToLowerPlatform(p2)) {
            p2.requestDropThrough(nowMillis);
            p2DropBufferedUntilMillis = 0L;
        }

        if (nowMillis > p1JumpBufferedUntilMillis) {
            p1JumpBufferedUntilMillis = 0L;
        }
        if (nowMillis > p2JumpBufferedUntilMillis) {
            p2JumpBufferedUntilMillis = 0L;
        }
        if (nowMillis > p1DropBufferedUntilMillis) {
            p1DropBufferedUntilMillis = 0L;
        }
        if (nowMillis > p2DropBufferedUntilMillis) {
            p2DropBufferedUntilMillis = 0L;
        }
    }

    private double axis(boolean negative, boolean positive) {
        if (negative == positive) {
            return 0.0;
        }
        return positive ? 1.0 : -1.0;
    }

    private void checkLandmineTriggers(long now) {
        for (Trap trap : traps) {
            if (!trap.isActive() || !(trap instanceof Landmine)) continue;

            boolean p1Step = trap.getBounds().intersects(p1.getBounds()) && !p1.isInvulnerable(now);
            boolean p2Step = trap.getBounds().intersects(p2.getBounds()) && !p2.isInvulnerable(now);

            if (p1Step || p2Step) {
                triggerExplosion(trap, now);
            }
        }
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

        for (Trap trap : traps) {
            if (trap.isActive() && meleeHitbox.intersects(trap.getBounds())) {
                SoundManager.getInstance().playEffect("melee");
                triggerExplosion(trap, now);
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
            if (!bullet.isActive()) continue;

            for (PlatformSurface surface : worldSurfaces) {
                if (bullet.getBounds().intersects(surface.getBounds())) {
                    var bulletBounds = bullet.getBounds();
                    addEffect("hit", bulletHitImage, bulletBounds.getMinX(), bulletBounds.getMinY(), 18, 18, 120L);
                    bullet.deactivate();
                    break;
                }
            }
            if (!bullet.isActive()) continue;

            for (Trap trap : traps) {
                if (trap.isActive() && bullet.getBounds().intersects(trap.getBounds())) {
                    bullet.deactivate();
                    triggerExplosion(trap, now);
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

    private void triggerExplosion(Trap trap, long now) {
        trap.deactivate();
        SoundManager.getInstance().playEffect("explosion");

        double forceModifier = trap.getExplosionForceMultiplier();
        triggerCameraShake(GameSettings.SCREEN_SHAKE_STRENGTH * 3.0 * forceModifier, (long)(350 * forceModifier));

        double centerX = trap.getBounds().getMinX() + trap.getBounds().getWidth() * 0.5;
        double centerY = trap.getBounds().getMinY() + trap.getBounds().getHeight() * 0.5;

        double effectSize = 180 * forceModifier;
        addEffect("explosion", explosionImage, centerX - (effectSize/2), centerY - (effectSize/2), effectSize, effectSize, 300L);

        applyExplosionForce(p1, centerX, centerY, now, forceModifier);
        applyExplosionForce(p2, centerX, centerY, now, forceModifier);
    }

    private void applyExplosionForce(Player player, double ex, double ey, long now, double trapForceModifier) {
        if (player.isInvulnerable(now)) return;

        var b = player.getBounds();
        double px = b.getMinX() + b.getWidth() * 0.5;
        double py = b.getMinY() + b.getHeight() * 0.5;

        double dx = px - ex;
        double dy = py - ey;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double radius = 170.0 * Math.max(1.0, trapForceModifier * 0.8);

        if (dist < radius) {
            double distanceMultiplier = 1.0 - (dist / radius);
            double normalizedX = dist > 0.0001 ? (dx / dist) : 0.0;
            double forceX = normalizedX * 1250.0 * distanceMultiplier * trapForceModifier;
            double forceY = (-750.0 * distanceMultiplier - 150.0) * trapForceModifier;

            player.applyKnockback(forceX, forceY);
            addEffect("blood", bloodImage, px - 15, py - 15, 30, 30, 250L);
        }
    }

    private boolean canDropToLowerPlatform(Player player) {
        return player.isOnGround();
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
        dropCoordinator.clearRoundItems();

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

            respawnPlayerFromSky(p1, now, -RESPAWN_PAIR_OFFSET, RESPAWN_PAIR_JITTER);
            respawnPlayerFromSky(p2, now, RESPAWN_PAIR_OFFSET, RESPAWN_PAIR_JITTER);
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
        return b.getMinY() > GameSettings.HEIGHT + margin;
    }

    private void renderGame() {
        long now = System.currentTimeMillis();

        gc.setFill(selectedMap.backgroundColor());
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
        if (SHOW_PLATFORM_GUIDES) {
            renderPlatformGuides();
        }

        List<Renderable> renderables = new ArrayList<>();
        renderables.add(p1);
        renderables.add(p2);
        renderables.addAll(bullets);
        renderables.addAll(traps);
        for (Renderable renderable : renderables) {
            renderable.render(gc);
        }

        dropCoordinator.renderWeaponDrops(gc);
        for (PowerUp p : powerUps) p.render(gc);

        for (GameEffect effect : hitEffects) {
            if ("doubleJump".equals(effect.type())) {
                renderDoubleJumpEffect(effect, now);
            } else if (effect.image() != EMPTY_IMAGE) {
                gc.drawImage(effect.image(), effect.x(), effect.y(), effect.width(), effect.height());
            } else if ("explosion".equals(effect.type())) {
                double t = Math.max(0, (double) (effect.expiresAt() - now) / effect.totalLife());
                gc.setFill(Color.web("#ff4400", t * 0.75));
                gc.fillOval(effect.x(), effect.y(), effect.width(), effect.height());
                gc.setFill(Color.web("#ffcc00", t * 0.95));
                gc.fillOval(
                        effect.x() + effect.width() * 0.25,
                        effect.y() + effect.height() * 0.25,
                        effect.width() * 0.5,
                        effect.height() * 0.5
                );
            }
        }
        gc.restore();

        renderBlastZoneWarning();

        gc.setStroke(Color.web("#2f3541"));
        gc.strokeRect(1, 1, GameSettings.WIDTH - 2, GameSettings.HEIGHT - 2);

        renderHud();
        renderCenterBanner(now);
    }

    private void renderDoubleJumpEffect(GameEffect effect, long nowMillis) {
        long totalLife = Math.max(1L, effect.totalLife());
        long remaining = Math.max(0L, effect.expiresAt() - nowMillis);
        double progress = 1.0 - (remaining / (double) totalLife);
        progress = Math.max(0.0, Math.min(1.0, progress));

        double scale = DOUBLE_JUMP_EFFECT_START_SCALE
                + ((DOUBLE_JUMP_EFFECT_END_SCALE - DOUBLE_JUMP_EFFECT_START_SCALE) * progress);
        double alpha = (1.0 - progress) * DOUBLE_JUMP_EFFECT_MAX_ALPHA;
        double pulse = 1.0 + (Math.sin(progress * Math.PI) * 0.15);

        double centerX = effect.x() + (effect.width() * 0.5);
        double centerY = effect.y() + (effect.height() * 0.5);
        double drawWidth = effect.width() * scale * pulse;
        double drawHeight = effect.height() * scale * pulse;
        double drawX = centerX - (drawWidth * 0.5);
        double drawY = centerY - (drawHeight * 0.5) - (progress * 10.0);

        if (alpha > 0.01) {
            gc.setFill(Color.web("#ffffff", alpha * 0.25));
            gc.fillOval(drawX - 6.0, drawY - 5.0, drawWidth + 12.0, drawHeight + 10.0);
            gc.setFill(Color.web("#ffffff", alpha * 0.55));
            gc.fillOval(
                    drawX + drawWidth * 0.18,
                    drawY + drawHeight * 0.20,
                    drawWidth * 0.64,
                    drawHeight * 0.54
            );
        }

        double ringAlpha = (1.0 - progress) * 0.85;
        if (ringAlpha > 0.01) {
            double ringWidth = drawWidth * (0.74 + (progress * 0.28));
            double ringHeight = drawHeight * (0.42 + (progress * 0.22));
            gc.setStroke(Color.web("#ffffff", ringAlpha));
            gc.setLineWidth(3.0 - (progress * 1.1));
            gc.strokeOval(
                    centerX - (ringWidth * 0.5),
                    centerY - (ringHeight * 0.5) + 6.0,
                    ringWidth,
                    ringHeight
            );
            gc.setStroke(Color.web("#ffffff", ringAlpha * 0.45));
            gc.setLineWidth(2.0 - (progress * 0.7));
            gc.strokeOval(
                    centerX - (ringWidth * 0.65),
                    centerY - (ringHeight * 0.65) + 6.0,
                    ringWidth * 1.30,
                    ringHeight * 1.30
            );
        }
    }

    private void renderExtendedMap() {
        selectedMap.render(gc, cameraX, cameraY, MAP_RENDER_EXTEND_MARGIN);
    }

    private void renderBlastZoneWarning() {
        gc.setFill(Color.web("#ff3344", 0.18));
        gc.fillRect(0, 0, GameSettings.WIDTH, 8);
        gc.fillRect(0, GameSettings.HEIGHT - 8, GameSettings.WIDTH, 8);
        gc.fillRect(0, 0, 8, GameSettings.HEIGHT);
        gc.fillRect(GameSettings.WIDTH - 8, 0, 8, GameSettings.HEIGHT);
    }

    private void updateCamera(double deltaSeconds) {
        trackedPlayerBounds.clear();
        for (Player player : trackedPlayers) {
            trackedPlayerBounds.add(player.getBounds());
        }

        sharedCamera.update(deltaSeconds, trackedPlayerBounds);
        cameraX = sharedCamera.getCameraX();
        cameraY = sharedCamera.getCameraY();
        cameraZoom = sharedCamera.getZoom();
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
        drawStocks(270, 63, p1Stocks, Color.web("#6ed4ff"), true);

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
        gc.save();
        gc.setFill(Color.web("#ffe175"));
        gc.setFont(Font.font("Impact", FontWeight.NORMAL, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("ROUND " + roundNumber, centerX + (centerWidth * 0.5), 34);
        gc.restore();
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        long secondsToDrop = Math.max(0L, (dropCoordinator.nextGunDropAtMillis() - now + 999) / 1000);
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

        gc.save();
        gc.setFill(Color.web("#fff4af"));
        gc.setFont(Font.font("Impact", FontWeight.NORMAL, 42));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(centerBannerText, x + (width * 0.5), y + (height * 0.5));
        gc.restore();
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
        pauseOverlay.hideAll();

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
            restartMatch();
        });

        Button backToMenu = new Button("Return to Menu");
        backToMenu.setPrefWidth(220);
        styleMenuButton(backToMenu, "#3a4354", "#252d39");
        backToMenu.setOnAction(event -> {
            SoundManager.getInstance().playEffect("click");
            backToMenu();
        });

        VBox modal = new VBox(12, title, score, restart, backToMenu);
        modal.setAlignment(Pos.CENTER);
        modal.setMaxWidth(420);
        modal.setMaxHeight(280);
        modal.setStyle("-fx-background-color: rgba(0,0,0,0.83); -fx-padding: 24; -fx-background-radius: 14;");
        getChildren().add(modal);
    }

    private void respawnPlayerFromSky(Player player, long nowMillis) {
        respawnPlayerFromSky(player, nowMillis, 0.0, RESPAWN_CENTER_SPREAD);
    }

    private void respawnPlayerFromSky(Player player, long nowMillis, double centerOffsetX, double jitterRange) {
        double centerSpawnX = ((GameSettings.WIDTH - GameSettings.PLAYER_WIDTH) * 0.5) + centerOffsetX;
        double jitter = (random.nextDouble() * 2.0 - 1.0) * Math.max(0.0, jitterRange);
        double minX = SIDE_RESPAWN_MARGIN;
        double maxX = GameSettings.WIDTH - GameSettings.PLAYER_WIDTH - SIDE_RESPAWN_MARGIN;
        double spawnX = Math.max(minX, Math.min(maxX, centerSpawnX + jitter));
        double spawnY = -(SKY_RESPAWN_Y_MIN + random.nextDouble() * (SKY_RESPAWN_Y_MAX - SKY_RESPAWN_Y_MIN));
        player.respawnFromSky(spawnX, spawnY, nowMillis);
    }

    private void spawnRoundPlayers(long nowMillis) {
        respawnPlayerFromSky(p1, nowMillis, -RESPAWN_PAIR_OFFSET, RESPAWN_PAIR_JITTER);
        respawnPlayerFromSky(p2, nowMillis, RESPAWN_PAIR_OFFSET, RESPAWN_PAIR_JITTER);
    }

    private void prepareRound(int targetRoundNumber) {
        long now = System.currentTimeMillis();
        roundNumber = targetRoundNumber;
        p1Stocks = GameSettings.STOCKS_PER_ROUND;
        p2Stocks = GameSettings.STOCKS_PER_ROUND;
        bullets.clear();
        hitEffects.clear();
        freezeUntilMillis = now + GameSettings.ROUND_START_COUNTDOWN_MS;
        dropCoordinator.prepareRound(freezeUntilMillis);
        spawnRoundPlayers(now);
        showCenterBanner("ROUND " + roundNumber, GameSettings.ROUND_START_COUNTDOWN_MS);
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
        pauseOverlay.showPaused(paused);
    }

    private void shutdownGameSystems() {
        gameLoop.stop();
        SoundManager.getInstance().stopBgm();
    }

    private void restartMatch() {
        shutdownGameSystems();
        onRematch.run();
    }

    private void backToMenu() {
        shutdownGameSystems();
        onBackToMenu.run();
    }

    private void addEffect(String type, Image image, double x, double y, double width, double height, long lifeMillis) {
        hitEffects.add(new GameEffect(type, image, x, y, width, height, System.currentTimeMillis() + lifeMillis, lifeMillis));
    }

    private void spawnDoubleJumpEffect(Player player) {
        if (player == null) {
            return;
        }
        var bounds = player.getBounds();
        double size = DOUBLE_JUMP_EFFECT_SIZE;
        double effectX = bounds.getMinX() + (bounds.getWidth() - size) * 0.5;
        double effectY = bounds.getMinY() + bounds.getHeight() - (size * 0.6);
        addEffect("doubleJump", EMPTY_IMAGE, effectX, effectY, size, size, DOUBLE_JUMP_EFFECT_LIFE_MS);
    }

    private void removeExpiredEffects(long nowMillis) {
        hitEffects.removeIf(effect -> effect.isExpired(nowMillis));
    }

    private void triggerCameraShake(double strength, long durationMillis) {
        double clampedStrength = Math.max(0.0, Math.min(GameSettings.SCREEN_SHAKE_MAX_STRENGTH, strength));
        shakeStrength = Math.max(shakeStrength, clampedStrength);
        shakeUntilMillis = Math.max(shakeUntilMillis, System.currentTimeMillis() + durationMillis);
    }

    private void handlePowerUpPickups(long nowMillis) {
        if (powerUps.isEmpty()) return;

        powerUps.removeIf(p -> {
            boolean p1Pickup = p.getBounds().intersects(p1.getBounds());
            boolean p2Pickup = p.getBounds().intersects(p2.getBounds());

            if (p1Pickup) {
                p.applyEffect(p1, nowMillis);
                SoundManager.getInstance().playEffect("pickup");
                addEffect("pickup", bulletHitImage, p.getBounds().getMinX(), p.getBounds().getMinY(), 24, 24, 160L);
                return true;
            } else if (p2Pickup) {
                p.applyEffect(p2, nowMillis);
                SoundManager.getInstance().playEffect("pickup");
                addEffect("pickup", bulletHitImage, p.getBounds().getMinX(), p.getBounds().getMinY(), 24, 24, 160L);
                return true;
            }
            return false;
        });
    }

    private void handleWeaponPickups(long nowMillis) {
        if (weaponDrops.isEmpty()) {
            return;
        }
        weaponDrops.removeIf(drop -> tryPickup(drop, p1, nowMillis) || tryPickup(drop, p2, nowMillis));
    }

    private boolean tryPickup(WeaponDrop drop, Player player, long nowMillis) {
        if (!drop.isLanded()) {
            return false;
        }
        if (!drop.bounds().intersects(player.getBounds())) {
            return false;
        }
        player.equipGun(drop.gun(), nowMillis);
        SoundManager.getInstance().playEffect("pickup");
        addEffect("pickup", bulletHitImage, drop.x() + 4, drop.y() + 4, 24, 24, 160L);
        return true;
    }
}
