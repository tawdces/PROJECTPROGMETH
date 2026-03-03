package game.entities;

import game.config.GameSettings;
import game.core.GameEntity;
import game.core.PlatformSurface;
import game.core.SpriteFrame;
import game.entities.weapons.Gun;
import game.entities.weapons.GunRegistry;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;

public abstract class Player extends GameEntity {

    private static final int MAX_JUMPS = 2;

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
    private long gunExpiresAtMillis;
    private Gun equippedGun = GunRegistry.UNARMED;
    private long nextActionAtMillis;
    private int jumpsUsed;

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
            Image raw = new Image(Objects.requireNonNull(Player.class.getResourceAsStream(spriteResourcePath)));
            this.spriteSheet = makeBackgroundTransparent(raw);
        }
    }

    public void setHorizontalInput(double horizontalInput) {
        this.horizontalInput = horizontalInput;
        if (horizontalInput > 0.01) {
            facingDirection = 1;
        } else if (horizontalInput < -0.01) {
            facingDirection = -1;
        }
    }

    public void jump() {
        if (onGround || jumpsUsed < MAX_JUMPS) {
            velocityY = GameSettings.JUMP_VELOCITY;
            onGround = false;
            jumpsUsed++;
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
        double drawX = x;
        double drawY = y;
        double drawWidth = width;
        double drawHeight = height;

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

        if (hasGun(System.currentTimeMillis())) {
            Image gunSprite = equippedGun.sprite();
            double gunWidth = equippedGun.renderWidth();
            double gunHeight = gunWidth * 0.5;
            double gunX = facingDirection > 0 ? drawX + drawWidth - 1 : drawX - (gunWidth - 1);
            double gunY = drawY + drawHeight / 2.0 - gunHeight / 2.0;

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

        gc.setFill(Color.WHITE);
        gc.fillText(name, drawX + 6, drawY - 6);
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
                jumpsUsed = 0;
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
        Gun gun = getEquippedGun(System.currentTimeMillis());
        if (gun == GunRegistry.UNARMED) {
            return List.of();
        }

        double bulletX = facingDirection > 0 ? x + width + 2 : x - GameSettings.BULLET_SIZE - 2;
        double bulletY = y + (height / 2.0) - (GameSettings.BULLET_SIZE / 2.0);
        return gun.fire(this, bulletX, bulletY, facingDirection);
    }

    public int getFacingDirection() {
        return facingDirection;
    }

    public boolean hasGun(long nowMillis) {
        return getEquippedGun(nowMillis) != GunRegistry.UNARMED;
    }

    public Gun getEquippedGun(long nowMillis) {
        if (nowMillis >= gunExpiresAtMillis) {
            equippedGun = GunRegistry.UNARMED;
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
        equippedGun = gun;
        gunExpiresAtMillis = gun == GunRegistry.UNARMED ? 0L : Long.MAX_VALUE;
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

    public void respawnFromSky(double spawnX, double spawnY) {
        x = spawnX;
        y = spawnY;
        previousX = spawnX;
        previousY = spawnY;
        horizontalInput = 0.0;
        knockbackVX = 0.0;
        velocityY = 0.0;
        onGround = false;
        dropThroughUntilMillis = 0L;
        jumpsUsed = 0;
    }
}
