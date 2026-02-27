package game.entities;

import game.config.GameSettings;
import game.core.GameEntity;
import game.core.PlatformSurface;
import game.core.SpriteFrame;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player extends GameEntity {

    private final String name;
    private final Color color;
    private final int facingDirection;
    private final Image spriteSheet;
    private final List<SpriteFrame> spriteFrames;

    private double horizontalInput;
    private double knockbackVX;
    private double velocityY;
    private double previousX;
    private double previousY;
    private boolean onGround;
    private long dropThroughUntilMillis;
    private long gunExpiresAtMillis;
    private WeaponType equippedWeapon = WeaponType.NONE;
    private long nextActionAtMillis;

    public Player(double startX, double startY, String name, Color color, int facingDirection) {
        super(startX, startY, GameSettings.PLAYER_WIDTH, GameSettings.PLAYER_HEIGHT);
        this.name = name;
        this.color = color;
        this.facingDirection = facingDirection;
        this.spriteSheet = null;
        this.spriteFrames = List.of();
    }

    public Player(double startX, double startY, String name, Color color, int facingDirection, String spriteResourcePath, List<SpriteFrame> spriteFrames) {
        super(startX, startY, GameSettings.PLAYER_WIDTH, GameSettings.PLAYER_HEIGHT);
        this.name = name;
        this.color = color;
        this.facingDirection = facingDirection;

        Image raw = new Image(Objects.requireNonNull(Player.class.getResourceAsStream(spriteResourcePath)));
        this.spriteSheet = makeBackgroundTransparent(raw);
        this.spriteFrames = spriteFrames;
    }

    public void setHorizontalInput(double horizontalInput) {
        this.horizontalInput = horizontalInput;
    }

    public void jump() {
        if (onGround) {
            velocityY = GameSettings.JUMP_VELOCITY;
            onGround = false;
        }
    }

    @Override
    public void update(double deltaSeconds) {
        previousX = x;
        previousY = y;

        x += (horizontalInput * GameSettings.MOVE_SPEED + knockbackVX) * deltaSeconds;
        y += velocityY * deltaSeconds;
        velocityY += GameSettings.GRAVITY * deltaSeconds;

        knockbackVX *= Math.pow(GameSettings.KNOCKBACK_DAMPING, deltaSeconds * 60.0);
    }

    @Override
    public void render(GraphicsContext gc) {
        if (spriteSheet != null) {
            SpriteFrame frame = selectFrame();
            gc.save();
            if (facingDirection < 0) {
                gc.translate(x + width, y);
                gc.scale(-1, 1);
                gc.drawImage(spriteSheet, frame.x(), frame.y(), frame.width(), frame.height(), 0, 0, width, height);
            } else {
                gc.drawImage(spriteSheet, frame.x(), frame.y(), frame.width(), frame.height(), x, y, width, height);
            }
            gc.restore();
        } else {
            gc.setFill(color);
            gc.fillRoundRect(x, y, width, height, 10, 10);
        }

        if (hasGun(System.currentTimeMillis())) {
            gc.setFill(Color.BLACK);
            double gunWidth = equippedWeapon == WeaponType.SHOTGUN ? 22 : (equippedWeapon == WeaponType.MACHINE_GUN ? 20 : 16);
            if (facingDirection > 0) {
                gc.fillRect(x + width - 2, y + height / 2.0 - 3, gunWidth, 6);
            } else {
                gc.fillRect(x - (gunWidth - 2), y + height / 2.0 - 3, gunWidth, 6);
            }
        }

        gc.setFill(Color.WHITE);
        gc.fillText(name, x + 6, y - 6);
    }

    private SpriteFrame selectFrame() {
        if (spriteFrames.isEmpty()) {
            return new SpriteFrame(0, 0, spriteSheet.getWidth(), spriteSheet.getHeight());
        }

        if (spriteFrames.size() == 1) {
            return spriteFrames.get(0);
        }

        if (!onGround) {
            return spriteFrames.get(Math.min(2, spriteFrames.size() - 1));
        }
        if (Math.abs(horizontalInput) > 0.01) {
            int frameIndex = (int) ((System.currentTimeMillis() / 95) % spriteFrames.size());
            return spriteFrames.get(frameIndex);
        }
        return spriteFrames.get(0);
    }

    private Image makeBackgroundTransparent(Image image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        WritableImage out = new WritableImage(w, h);

        PixelReader reader = image.getPixelReader();
        PixelWriter writer = out.getPixelWriter();
        Color key = reader.getColor(0, 0);
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                Color c = reader.getColor(px, py);
                if (colorDistance(c, key) < 0.14) {
                    writer.setColor(px, py, Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.0));
                } else {
                    writer.setColor(px, py, c);
                }
            }
        }
        return out;
    }

    private double colorDistance(Color a, Color b) {
        double dr = a.getRed() - b.getRed();
        double dg = a.getGreen() - b.getGreen();
        double db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    public boolean isMeleeHit(Player other) {
        Rectangle2D hitbox;
        if (facingDirection > 0) {
            hitbox = new Rectangle2D(x + width, y + 8, GameSettings.MELEE_RANGE, height - 16);
        } else {
            hitbox = new Rectangle2D(x - GameSettings.MELEE_RANGE, y + 8, GameSettings.MELEE_RANGE, height - 16);
        }
        return hitbox.intersects(other.getBounds());
    }

    public void applyKnockback(double forceX, double forceY) {
        knockbackVX += forceX;
        velocityY += forceY;
    }

    public void resolveCollisions(List<PlatformSurface> surfaces, long nowMillis) {
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
            } else if (!surfaceData.isOneWay() && previousY >= surface.getMaxY()) {
                y = surface.getMaxY();
                if (velocityY < 0) {
                    velocityY = 0.0;
                }
            }
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
        WeaponType weapon = getEquippedWeapon(System.currentTimeMillis());
        if (weapon == WeaponType.NONE) {
            return List.of();
        }

        double bulletX = facingDirection > 0 ? x + width + 2 : x - GameSettings.BULLET_SIZE - 2;
        double bulletY = y + (height / 2.0) - (GameSettings.BULLET_SIZE / 2.0);
        double speedX = facingDirection * weapon.bulletSpeed();
        double forceX = facingDirection * weapon.forceX();

        if (weapon == WeaponType.SHOTGUN) {
            double[] verticalSpeeds = {-120.0, -60.0, 0.0, 60.0, 120.0};
            List<Bullet> shotgunBullets = new ArrayList<>();
            for (double verticalSpeed : verticalSpeeds) {
                shotgunBullets.add(new Bullet(
                        bulletX,
                        bulletY,
                        speedX,
                        verticalSpeed,
                        forceX,
                        weapon.forceY(),
                        this
                ));
            }
            return shotgunBullets;
        }

        return List.of(new Bullet(
                bulletX,
                bulletY,
                speedX,
                0,
                forceX,
                weapon.forceY(),
                this
        ));
    }

    public int getFacingDirection() {
        return facingDirection;
    }

    public boolean hasGun(long nowMillis) {
        return getEquippedWeapon(nowMillis) != WeaponType.NONE;
    }

    public WeaponType getEquippedWeapon(long nowMillis) {
        if (nowMillis >= gunExpiresAtMillis) {
            equippedWeapon = WeaponType.NONE;
        }
        return equippedWeapon;
    }

    public void equipGun(WeaponType weaponType, long nowMillis) {
        equippedWeapon = weaponType;
        gunExpiresAtMillis = nowMillis + weaponType.durationMillis();
    }

    public long getGunRemainingMillis(long nowMillis) {
        if (getEquippedWeapon(nowMillis) == WeaponType.NONE) {
            return 0;
        }
        return Math.max(0, gunExpiresAtMillis - nowMillis);
    }

    public long getShootCooldownMillis(long nowMillis) {
        WeaponType weapon = getEquippedWeapon(nowMillis);
        if (weapon == WeaponType.NONE) {
            return GameSettings.SHOOT_COOLDOWN_MS;
        }
        return weapon.cooldownMillis();
    }

    public boolean canAction(long nowMillis) {
        return nowMillis >= nextActionAtMillis;
    }

    public void setActionCooldown(long nowMillis, long cooldownMillis) {
        nextActionAtMillis = nowMillis + cooldownMillis;
    }
}
