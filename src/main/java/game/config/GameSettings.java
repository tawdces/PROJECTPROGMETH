package game.config;

/**
 * Represents the game settings.
 */
public final class GameSettings {

    /**
     * Constant for width.
     */
    public static final double WIDTH = 960;
    /**
     * Constant for height.
     */
    public static final double HEIGHT = 540;
    /**
     * Constant for ground y.
     */
    public static final double GROUND_Y = HEIGHT - 70;

    /**
     * Constant for player width.
     */
    public static final double PLAYER_WIDTH = 44;
    /**
     * Constant for player height.
     */
    public static final double PLAYER_HEIGHT = 68;
    /**
     * Constant for bullet size.
     */
    public static final double BULLET_SIZE = 14;
    /**
     * Constant for box size.
     */
    public static final double BOX_SIZE = 34;

    /**
     * Constant for move speed.
     */
    public static final double MOVE_SPEED = 320.0;
    /**
     * Constant for knockback damping.
     */
    public static final double KNOCKBACK_DAMPING = 0.94;
    /**
     * Constant for melee range.
     */
    public static final double MELEE_RANGE = 30.0;
    /**
     * Constant for gravity.
     */
    public static final double GRAVITY = 1500.0;
    /**
     * Constant for jump velocity.
     */
    public static final double JUMP_VELOCITY = -620.0;
    /**
     * Constant for max air jumps.
     */
    public static final int MAX_AIR_JUMPS = 1;
    /**
     * Constant for coyote time ms.
     */
    public static final long COYOTE_TIME_MS = 90;
    /**
     * Constant for jump input buffer ms.
     */
    public static final long JUMP_INPUT_BUFFER_MS = 120;
    /**
     * Constant for drop input buffer ms.
     */
    public static final long DROP_INPUT_BUFFER_MS = 140;
    /**
     * Constant for camera dynamic zoom.
     */
    public static final boolean CAMERA_DYNAMIC_ZOOM = false;
    /**
     * Constant for camera fixed zoom.
     */
    public static final double CAMERA_FIXED_ZOOM = 1.0;
    /**
     * Constant for camera min zoom.
     */
    public static final double CAMERA_MIN_ZOOM = 0.92;
    /**
     * Constant for camera max zoom.
     */
    public static final double CAMERA_MAX_ZOOM = 1.90;
    /**
     * Constant for camera padding x.
     */
    public static final double CAMERA_PADDING_X = 260.0;
    /**
     * Constant for camera padding y.
     */
    public static final double CAMERA_PADDING_Y = 180.0;
    /**
     * Constant for camera follow speed.
     */
    public static final double CAMERA_FOLLOW_SPEED = 8.0;
    /**
     * Constant for world floor y.
     */
    public static final double WORLD_FLOOR_Y = HEIGHT + 130.0;
    /**
     * Constant for blast zone margin.
     */
    public static final double BLAST_ZONE_MARGIN = 120.0;

    /**
     * Constant for melee force.
     */
    public static final double MELEE_FORCE = 520.0;
    /**
     * Constant for melee vertical force.
     */
    public static final double MELEE_VERTICAL_FORCE = -65.0;
    /**
     * Constant for bullet force.
     */
    public static final double BULLET_FORCE = 640.0;
    /**
     * Constant for bullet vertical force.
     */
    public static final double BULLET_VERTICAL_FORCE = -55.0;
    /**
     * Constant for bullet speed.
     */
    public static final double BULLET_SPEED = 620.0;
    /**
     * Constant for shotgun pellet range.
     */
    public static final double SHOTGUN_PELLET_RANGE = 360.0;
    /**
     * Constant for shoot recoil base.
     */
    public static final double SHOOT_RECOIL_BASE = 42.0;
    /**
     * Constant for shoot recoil per bullet.
     */
    public static final double SHOOT_RECOIL_PER_BULLET = 22.0;
    /**
     * Constant for shoot recoil vertical force.
     */
    public static final double SHOOT_RECOIL_VERTICAL_FORCE = -16.0;

    /**
     * Constant for gun duration ms.
     */
    public static final long GUN_DURATION_MS = 15_000;
    /**
     * Constant for first drop delay ms.
     */
    public static final long FIRST_DROP_DELAY_MS = 5_000;
    /**
     * Constant for next drop interval ms.
     */
    public static final long NEXT_DROP_INTERVAL_MS = 12_000;
    /**
     * Constant for shoot cooldown ms.
     */
    public static final long SHOOT_COOLDOWN_MS = 220;
    /**
     * Constant for melee cooldown ms.
     */
    public static final long MELEE_COOLDOWN_MS = 250;
    /**
     * Constant for respawn invulnerability ms.
     */
    public static final long RESPAWN_INVULNERABILITY_MS = 1_200;
    /**
     * Constant for stocks per round.
     */
    public static final int STOCKS_PER_ROUND = 3;
    /**
     * Constant for round wins to match.
     */
    public static final int ROUND_WINS_TO_MATCH = 3;
    /**
     * Constant for round end delay ms.
     */
    public static final long ROUND_END_DELAY_MS = 1_500;
    /**
     * Constant for round start countdown ms.
     */
    public static final long ROUND_START_COUNTDOWN_MS = 1_600;
    /**
     * Constant for screen shake duration ms.
     */
    public static final long SCREEN_SHAKE_DURATION_MS = 90;
    /**
     * Constant for screen shake strength.
     */
    public static final double SCREEN_SHAKE_STRENGTH = 3.2;
    /**
     * Constant for screen shake max strength.
     */
    public static final double SCREEN_SHAKE_MAX_STRENGTH = 5.0;

    /**
     * Constant for barrel size.
     */
    public static final double BARREL_SIZE = 52.0;
    /**
     * Constant for explosion radius.
     */
    public static final double EXPLOSION_RADIUS = 200.0;
    /**
     * Constant for explosion force base.
     */
    public static final double EXPLOSION_FORCE_BASE = 1400.0;
    /**
     * Constant for explosion vertical force.
     */
    public static final double EXPLOSION_VERTICAL_FORCE = -450.0;

    /**
     * Constant for trap drop interval ms.
     */
    public static final long TRAP_DROP_INTERVAL_MS = 8_000;
    /**
     * Constant for powerup drop interval ms.
     */
    public static final long POWERUP_DROP_INTERVAL_MS = 20_000;

    /**
     * Creates a private game settings instance.
     */
    private GameSettings() {
    }
}
