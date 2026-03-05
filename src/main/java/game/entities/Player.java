package game.entities;

import game.config.GameSettings;
import game.map.PlatformSurface;
import game.logic.SoundManager;
import game.logic.SpriteFrame;
import game.entities.weapons.Gun;
import game.entities.weapons.GunRegistry;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class Player extends GameEntity {

    private static final double GUN_WIDTH_RATIO = 1.80;
    private static final double GUN_HEIGHT_RATIO = 0.40;
    private static final double GUN_RENDER_SCALE = 1.22;
    private static final Image EMPTY_SPRITE = new WritableImage(1, 1);

    private final String name;
    private final Color color;
    private int facingDirection;
    private final Image spriteSheet;
    private final List<SpriteFrame> spriteFrames;

    private double horizontalInput;
    private double knockbackVX;
    private double velocityY;
    private double previousX;
    private double previousY;
    private boolean onGround;
    private long dropThroughUntilMillis;
    private long coyoteJumpUntilMillis;
    private long gunExpiresAtMillis;
    private Gun fallbackGun = GunRegistry.UNARMED;
    private Gun equippedGun = GunRegistry.UNARMED;
    private long nextActionAtMillis;
    private int airJumpsRemaining = GameSettings.MAX_AIR_JUMPS;


    private long invulnerableUntilMillis;
    private double currentSpeedMultiplier = 1.0;
    private long speedBoostUntilMillis;

    private double stepDistance;
    private static final double STEP_INTERVAL = 60.0;

    protected Player(
            double startX,
            double startY,
            String name,
            Color color,
            int initialFacingDirection,
            String spriteResourcePath,
            List<SpriteFrame> spriteFrames
    ) {
        super(startX, startY, GameSettings.PLAYER_WIDTH, GameSettings.PLAYER_HEIGHT);
        this.name = name;
        this.color = color;
        this.facingDirection = initialFacingDirection;
        this.spriteFrames = spriteFrames;

        if (spriteResourcePath == null) {
            this.spriteSheet = null;
        } else {
            this.spriteSheet = loadSprite(spriteResourcePath);
        }
    }

    private static Image loadSprite(String spriteResourcePath) {
        var resourceStream = Player.class.getResourceAsStream(spriteResourcePath);
        if (resourceStream != null) {
            Image image = new Image(resourceStream);
            if (!image.isError()) {
                return image;
            }
        }

        Path fallback = Paths.get("src", "main", "resources", spriteResourcePath.replaceFirst("^/", ""));
        if (Files.exists(fallback)) {
            Image image = new Image(fallback.toUri().toString(), false);
            if (!image.isError()) {
                return image;
            }
        }
        return EMPTY_SPRITE;
    }

    public void setHorizontalInput(double horizontalInput) {
        this.horizontalInput = horizontalInput;
        if (horizontalInput > 0.01) {
            facingDirection = 1;
        } else if (horizontalInput < -0.01) {
            facingDirection = -1;
        }
    }

    public boolean jump(long nowMillis) {
        boolean canGroundJump = onGround || nowMillis <= coyoteJumpUntilMillis;
        if (canGroundJump) {
            velocityY = GameSettings.JUMP_VELOCITY;
            onGround = false;
            coyoteJumpUntilMillis = 0L;
            airJumpsRemaining = GameSettings.MAX_AIR_JUMPS;
            return true;
        }

        if (airJumpsRemaining > 0) {
            velocityY = GameSettings.JUMP_VELOCITY;
            airJumpsRemaining--;
            return true;
        }
        return false;
    }

    @Override
    public void update(double deltaSeconds) {
        long nowMillis = System.currentTimeMillis();
        previousX = x;
        previousY = y;


        if (nowMillis > speedBoostUntilMillis) {
            currentSpeedMultiplier = 1.0;
        }


        double moveX = horizontalInput * (GameSettings.MOVE_SPEED * currentSpeedMultiplier) * deltaSeconds;
        x += moveX + (knockbackVX * deltaSeconds);
        y += velocityY * deltaSeconds;
        velocityY += GameSettings.GRAVITY * deltaSeconds;

        knockbackVX *= Math.pow(GameSettings.KNOCKBACK_DAMPING, deltaSeconds * 60.0);

        if (onGround && Math.abs(horizontalInput) > 0.01) {
            stepDistance += Math.abs(moveX);
            if (stepDistance > STEP_INTERVAL) {
                SoundManager.getInstance().playEffect("step");
                stepDistance = 0.0;
            }
        } else if (!onGround) {
            stepDistance = STEP_INTERVAL;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        long nowMillis = System.currentTimeMillis();
        boolean invulnerable = isInvulnerable(nowMillis);
        boolean speedBoosted = isSpeedBoosted(nowMillis);


        boolean blink = invulnerable && ((nowMillis / 85L) % 2L == 0L);

        double drawX = x;
        double drawY = y;
        double drawWidth = width;
        double drawHeight = height;

        gc.save();
        gc.setGlobalAlpha(blink ? 0.48 : 1.0);

        if (spriteSheet != null) {
            SpriteFrame frame = selectFrame();
            gc.save();
            if (facingDirection < 0) {
                gc.translate(drawX + drawWidth, drawY);
                gc.scale(-1, 1);
                gc.drawImage(spriteSheet, frame.x(), frame.y(), frame.width(), frame.height(), 0, 0, drawWidth, drawHeight);
            } else {
                gc.drawImage(spriteSheet, frame.x(), frame.y(), frame.width(), frame.height(), drawX, drawY, drawWidth, drawHeight);
            }
            gc.restore();
        } else {
            gc.setFill(color);
            gc.fillRoundRect(drawX, drawY, drawWidth, drawHeight, 10, 10);
        }

        if (hasGun(nowMillis)) {
            Image gunSprite = equippedGun.sprite();
            GunPose pose = computeGunPose(equippedGun);
            double gunX = pose.x();
            double gunY = pose.y();
            double gunWidth = pose.width();
            double gunHeight = pose.height();

            gc.save();
            if (facingDirection < 0) {
                gc.translate(gunX + gunWidth, gunY);
                gc.scale(-1, 1);
                gc.drawImage(gunSprite, 0, 0, gunWidth, gunHeight);
            } else {
                gc.drawImage(gunSprite, gunX, gunY, gunWidth, gunHeight);
            }
            gc.restore();
        }
        gc.restore();


        if (invulnerable || speedBoosted) {
            gc.setLineWidth(2.5);

            if (invulnerable && speedBoosted) {
                gc.setStroke(Color.web("#9cffad", 0.90));
            } else if (speedBoosted) {
                gc.setStroke(Color.web("#ffeb6b", 0.90));
            } else {
                gc.setStroke(Color.web("#9ce8ff", 0.90));
            }


            double offset = speedBoosted ? (Math.random() * 2 - 1) : 0;
            gc.strokeOval(drawX - 3 + offset, drawY - 4 + offset, drawWidth + 6, drawHeight + 8);
        }


        gc.setFill(Color.color(0.06, 0.08, 0.10, 0.65));
        gc.fillRoundRect(drawX + 4, drawY - 19, 30, 14, 8, 8);
        gc.setFill(Color.WHITE);
        gc.fillText(name, drawX + 9, drawY - 8);
    }

    private SpriteFrame selectFrame() {
        if (spriteSheet == null) {
            return new SpriteFrame(0, 0, width, height);
        }
        if (spriteFrames.isEmpty()) {
            return new SpriteFrame(0, 0, spriteSheet.getWidth(), spriteSheet.getHeight());
        }
        return spriteFrames.get(0);
    }

    public Rectangle2D getMeleeHitbox() {
        if (facingDirection > 0) {
            return new Rectangle2D(x + width, y + 8, GameSettings.MELEE_RANGE, height - 16);
        } else {
            return new Rectangle2D(x - GameSettings.MELEE_RANGE, y + 8, GameSettings.MELEE_RANGE, height - 16);
        }
    }

    public boolean isMeleeHit(Player other) {
        return getMeleeHitbox().intersects(other.getBounds());
    }

    public void applyKnockback(double forceX, double forceY) {
        knockbackVX += forceX;
        velocityY += forceY;
    }


    public void applyShield(long durationMillis, long nowMillis) {

        this.invulnerableUntilMillis = Math.max(this.invulnerableUntilMillis, nowMillis) + durationMillis;
    }

    public void applySpeedBoost(double multiplier, long durationMillis, long nowMillis) {
        this.currentSpeedMultiplier = multiplier;
        this.speedBoostUntilMillis = Math.max(this.speedBoostUntilMillis, nowMillis) + durationMillis;
    }

    public void resolveCollisions(List<PlatformSurface> surfaces, long nowMillis) {
        boolean wasOnGround = onGround;
        onGround = false;

        for (PlatformSurface surfaceData : surfaces) {
            if (surfaceData.isOneWay() && nowMillis < dropThroughUntilMillis) {
                continue;
            }

            Rectangle2D surface = surfaceData.getBounds();
            Rectangle2D bounds = getBounds();
            if (!bounds.intersects(surface)) {
                continue;
            }

            double prevBottom = previousY + height;
            if (velocityY >= 0.0 && prevBottom <= surface.getMinY() + 0.5) {
                y = surface.getMinY() - height;
                velocityY = 0.0;
                onGround = true;
                airJumpsRemaining = GameSettings.MAX_AIR_JUMPS;
                coyoteJumpUntilMillis = nowMillis + GameSettings.COYOTE_TIME_MS;
            } else if (!surfaceData.isOneWay() && previousY >= surface.getMaxY()) {
                y = surface.getMaxY();
                if (velocityY < 0) {
                    velocityY = 0.0;
                }
            }
        }

        if (!onGround && wasOnGround) {
            coyoteJumpUntilMillis = nowMillis + GameSettings.COYOTE_TIME_MS;
        }
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void requestDropThrough(long nowMillis) {
        if (!onGround) {
            return;
        }
        dropThroughUntilMillis = nowMillis + 260;
        onGround = false;
        coyoteJumpUntilMillis = 0L;
        if (velocityY < 120) {
            velocityY = 120;
        }
        y += 2;
    }

    public void clampX(double minX, double maxX) {
        if (x < minX) {
            x = minX;
            knockbackVX = Math.max(0.0, knockbackVX);
        } else if (x > maxX) {
            x = maxX;
            knockbackVX = Math.min(0.0, knockbackVX);
        }
    }

    public List<Bullet> shoot() {
        long nowMillis = System.currentTimeMillis();
        Gun gun = getEquippedGun(nowMillis);
        if (gun == GunRegistry.UNARMED) {
            return List.of();
        }

        GunPose pose = computeGunPose(gun);
        return gun.fire(this, pose.muzzleX(), pose.muzzleY(), facingDirection);
    }

    public int getFacingDirection() {
        return facingDirection;
    }

    public boolean hasGun(long nowMillis) {
        return getEquippedGun(nowMillis) != GunRegistry.UNARMED;
    }

    public Gun getEquippedGun(long nowMillis) {
        if (nowMillis >= gunExpiresAtMillis) {
            equippedGun = fallbackGun;
        }
        return equippedGun;
    }

    public void equipGun(Gun gun, long nowMillis) {
        equippedGun = gun;
        if (gun == GunRegistry.UNARMED) {
            gunExpiresAtMillis = 0L;
            return;
        }
        gunExpiresAtMillis = nowMillis + gun.durationMillis();
    }

    public void equipPermanentGun(Gun gun) {
        fallbackGun = gun;
        equippedGun = gun;
        gunExpiresAtMillis = Long.MAX_VALUE;
    }

    public long getShootCooldownMillis(long nowMillis) {
        Gun gun = getEquippedGun(nowMillis);
        if (gun == GunRegistry.UNARMED) {
            return GameSettings.SHOOT_COOLDOWN_MS;
        }
        return gun.cooldownMillis();
    }

    public boolean canAction(long nowMillis) {
        return nowMillis >= nextActionAtMillis;
    }

    public void setActionCooldown(long nowMillis, long cooldownMillis) {
        nextActionAtMillis = nowMillis + cooldownMillis;
    }

    public boolean isInvulnerable(long nowMillis) {
        return nowMillis < invulnerableUntilMillis;
    }

    public boolean isSpeedBoosted(long nowMillis) {
        return nowMillis < speedBoostUntilMillis;
    }

    private GunPose computeGunPose(Gun gun) {
        double gunWidth = Math.min(gun.renderWidth() * GUN_RENDER_SCALE, width * GUN_WIDTH_RATIO);
        double gunHeight = Math.max(14.0, gunWidth * GUN_HEIGHT_RATIO);
        double handAnchorX = facingDirection > 0 ? x + width * 0.58 : x + width * 0.42;
        double handAnchorY = y + height * 0.56;
        double gunX = facingDirection > 0 ? handAnchorX - gunWidth * 0.20 : handAnchorX - gunWidth * 0.80;
        double gunY = handAnchorY - gunHeight * 0.58;

        double muzzleX = facingDirection > 0
                ? gunX + gunWidth - 2.0
                : gunX + 2.0 - GameSettings.BULLET_SIZE;
        double muzzleY = gunY + gunHeight * 0.50 - (GameSettings.BULLET_SIZE * 0.5);
        return new GunPose(gunX, gunY, gunWidth, gunHeight, muzzleX, muzzleY);
    }

    public void respawnFromSky(double spawnX, double spawnY, long nowMillis) {
        x = spawnX;
        y = spawnY;
        previousX = spawnX;
        previousY = spawnY;
        horizontalInput = 0.0;
        knockbackVX = 0.0;
        velocityY = 0.0;
        onGround = false;
        dropThroughUntilMillis = 0L;
        coyoteJumpUntilMillis = 0L;
        airJumpsRemaining = GameSettings.MAX_AIR_JUMPS;
        nextActionAtMillis = nowMillis + 180L;
        currentSpeedMultiplier = 1.0;
        speedBoostUntilMillis = 0L;
        invulnerableUntilMillis = nowMillis + GameSettings.RESPAWN_INVULNERABILITY_MS;
    }

    private record GunPose(double x, double y, double width, double height, double muzzleX, double muzzleY) {
    }
}
