package game.entities;

import javafx.scene.paint.Color;

public enum WeaponType {
    NONE("OFF", Color.TRANSPARENT, 0, 0, 0, 0, 0, 0),
    PISTOL("Pistol", Color.GOLD, 18_000, 240, 620.0, 620.0, -55.0, 1),
    RIFLE("Rifle", Color.DEEPSKYBLUE, 22_000, 150, 760.0, 560.0, -45.0, 1),
    MACHINE_GUN("Machine", Color.ORANGERED, 16_000, 90, 690.0, 500.0, -35.0, 1),
    SHOTGUN("Shotgun", Color.LIMEGREEN, 14_000, 430, 560.0, 460.0, -35.0, 5);

    private final String label;
    private final Color boxColor;
    private final long durationMillis;
    private final long cooldownMillis;
    private final double bulletSpeed;
    private final double forceX;
    private final double forceY;
    private final int pelletCount;

    WeaponType(
            String label,
            Color boxColor,
            long durationMillis,
            long cooldownMillis,
            double bulletSpeed,
            double forceX,
            double forceY,
            int pelletCount
    ) {
        this.label = label;
        this.boxColor = boxColor;
        this.durationMillis = durationMillis;
        this.cooldownMillis = cooldownMillis;
        this.bulletSpeed = bulletSpeed;
        this.forceX = forceX;
        this.forceY = forceY;
        this.pelletCount = pelletCount;
    }

    public String label() {
        return label;
    }

    public Color boxColor() {
        return boxColor;
    }

    public long durationMillis() {
        return durationMillis;
    }

    public long cooldownMillis() {
        return cooldownMillis;
    }

    public double bulletSpeed() {
        return bulletSpeed;
    }

    public double forceX() {
        return forceX;
    }

    public double forceY() {
        return forceY;
    }

    public int pelletCount() {
        return pelletCount;
    }
}
