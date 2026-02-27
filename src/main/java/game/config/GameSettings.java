package game.config;

public final class GameSettings {

    public static final double WIDTH = 960;
    public static final double HEIGHT = 540;
    public static final double GROUND_Y = HEIGHT - 70;

    public static final double PLAYER_WIDTH = 44;
    public static final double PLAYER_HEIGHT = 68;
    public static final double BULLET_SIZE = 10;
    public static final double BOX_SIZE = 34;

    public static final double MOVE_SPEED = 260.0;
    public static final double KNOCKBACK_DAMPING = 0.90;
    public static final double MELEE_RANGE = 26.0;
    public static final double GRAVITY = 1300.0;
    public static final double JUMP_VELOCITY = -560.0;
    public static final double WORLD_FLOOR_Y = HEIGHT + 100.0;

    public static final double MELEE_FORCE = 420.0;
    public static final double MELEE_VERTICAL_FORCE = -40.0;
    public static final double BULLET_FORCE = 640.0;
    public static final double BULLET_VERTICAL_FORCE = -55.0;
    public static final double BULLET_SPEED = 620.0;

    public static final long GUN_DURATION_MS = 25_000;
    public static final long FIRST_DROP_DELAY_MS = 10_000;
    public static final long NEXT_DROP_INTERVAL_MS = 30_000;
    public static final long SHOOT_COOLDOWN_MS = 220;
    public static final long MELEE_COOLDOWN_MS = 280;

    private GameSettings() {
    }
}
