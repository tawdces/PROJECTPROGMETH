package game.config;

public final class GameSettings {

    public static final double WIDTH = 960;
    public static final double HEIGHT = 540;
    public static final double GROUND_Y = HEIGHT - 70;

    public static final double PLAYER_WIDTH = 44;
    public static final double PLAYER_HEIGHT = 68;
    public static final double BULLET_SIZE = 14;
    public static final double BOX_SIZE = 34;

    public static final double MOVE_SPEED = 320.0;
    public static final double KNOCKBACK_DAMPING = 0.94;
    public static final double MELEE_RANGE = 30.0;
    public static final double GRAVITY = 1500.0;
    public static final double JUMP_VELOCITY = -620.0;
    public static final int MAX_AIR_JUMPS = 1;
    public static final long COYOTE_TIME_MS = 90;
    public static final long JUMP_INPUT_BUFFER_MS = 120;
    public static final long DROP_INPUT_BUFFER_MS = 140;
    public static final boolean CAMERA_DYNAMIC_ZOOM = false;
    public static final double CAMERA_FIXED_ZOOM = 1.0;
    public static final double CAMERA_MIN_ZOOM = 0.92;
    public static final double CAMERA_MAX_ZOOM = 1.90;
    public static final double CAMERA_PADDING_X = 260.0;
    public static final double CAMERA_PADDING_Y = 180.0;
    public static final double CAMERA_FOLLOW_SPEED = 8.0;
    public static final double WORLD_FLOOR_Y = HEIGHT + 130.0;
    public static final double BLAST_ZONE_MARGIN = 120.0;

    public static final double MELEE_FORCE = 520.0;
    public static final double MELEE_VERTICAL_FORCE = -65.0;
    public static final double BULLET_FORCE = 640.0;
    public static final double BULLET_VERTICAL_FORCE = -55.0;
    public static final double BULLET_SPEED = 620.0;
    public static final double SHOTGUN_PELLET_RANGE = 360.0;
    public static final double SHOOT_RECOIL_BASE = 42.0;
    public static final double SHOOT_RECOIL_PER_BULLET = 22.0;
    public static final double SHOOT_RECOIL_VERTICAL_FORCE = -16.0;

    public static final long GUN_DURATION_MS = 15_000;
    public static final long FIRST_DROP_DELAY_MS = 5_000;
    public static final long NEXT_DROP_INTERVAL_MS = 12_000;
    public static final long SHOOT_COOLDOWN_MS = 220;
    public static final long MELEE_COOLDOWN_MS = 250;
    public static final long RESPAWN_INVULNERABILITY_MS = 1_200;
    public static final int STOCKS_PER_ROUND = 3;
    public static final int ROUND_WINS_TO_MATCH = 3;
    public static final long ROUND_END_DELAY_MS = 1_500;
    public static final long ROUND_START_COUNTDOWN_MS = 1_600;
    public static final long SCREEN_SHAKE_DURATION_MS = 90;
    public static final double SCREEN_SHAKE_STRENGTH = 3.2;
    public static final double SCREEN_SHAKE_MAX_STRENGTH = 5.0;

    public static final double BARREL_SIZE = 52.0;
    public static final double EXPLOSION_RADIUS = 200.0;
    public static final double EXPLOSION_FORCE_BASE = 1400.0;
    public static final double EXPLOSION_VERTICAL_FORCE = -450.0;

    public static final long TRAP_DROP_INTERVAL_MS = 8_000;
    public static final long POWERUP_DROP_INTERVAL_MS = 20_000;

    private GameSettings() {
    }
}
