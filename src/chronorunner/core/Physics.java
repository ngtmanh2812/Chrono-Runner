package chronorunner.core;

/**
 * Toàn bộ hằng số tinh chỉnh của trò chơi nằm ở một chỗ.
 *
 * <p>Việc gom lại như vậy giúp cân bằng gameplay không phải đụng tới
 * logic ở nhiều lớp khác nhau.</p>
 */
public final class Physics {

    private Physics() {
    }

    /** Kích thước một ô bản đồ (pixel). */
    public static final int TILE_SIZE = 32;

    /** Số bước vật lý mỗi giây. */
    public static final double FIXED_DT = 1.0 / 60.0;

    // ---- Trọng lực ---------------------------------------------------------
    public static final double GRAVITY = 2200;
    public static final double MAX_FALL_SPEED = 1150;

    // ---- Người chơi: di chuyển ngang ---------------------------------------
    public static final double PLAYER_RUN_SPEED = 268;
    public static final double PLAYER_GROUND_ACCEL = 2400;
    public static final double PLAYER_AIR_ACCEL = 1500;
    public static final double PLAYER_GROUND_FRICTION = 2800;
    public static final double PLAYER_ICE_FRICTION = 300;

    // ---- Người chơi: nhảy --------------------------------------------------
    public static final double JUMP_VELOCITY = -690;
    public static final double DOUBLE_JUMP_VELOCITY = -600;
    /** Nhả phím nhảy sớm thì cắt bớt lực lên để nhảy thấp. */
    public static final double JUMP_CUT_MULTIPLIER = 0.42;
    /** Khoảng "ân hạn" sau khi rời mép vẫn nhảy được. */
    public static final double COYOTE_TIME = 0.10;
    /** Nhấn nhảy trước khi chạm đất vẫn được ghi nhận. */
    public static final double JUMP_BUFFER_TIME = 0.12;
    /** Nhảy đè lên đầu kẻ địch thì nảy lên. */
    public static final double STOMP_BOUNCE = -430;

    // ---- Dash --------------------------------------------------------------
    public static final double DASH_SPEED = 640;
    public static final double DASH_DURATION = 0.18;
    public static final double DASH_ENERGY_COST = 15;

    // ---- Chiến đấu ---------------------------------------------------------
    public static final double ATTACK_DURATION = 0.14;
    public static final double ATTACK_COOLDOWN = 0.34;
    public static final double ATTACK_REACH = 30;
    public static final int ATTACK_DAMAGE = 1;
    public static final double INVULN_DURATION = 1.1;
    public static final double KNOCKBACK_X = 230;
    public static final double KNOCKBACK_Y = -270;

    // ---- Năng lượng thời gian ----------------------------------------------
    public static final double ENERGY_REGEN_PER_SECOND = 7.5;
    public static final double REWIND_ENERGY_COST = 30;
    public static final double FREEZE_ENERGY_COST = 45;
    /** Số giây để "cuộn" hình ảnh khi tua ngược. */
    public static final double REWIND_PLAYBACK_SPEED = 4.5;

    // ---- Kẻ địch -----------------------------------------------------------
    public static final double ENEMY_PATROL_SPEED = 62;
    public static final double BAT_CHASE_SPEED = 118;
    public static final double GUARDIAN_CHASE_SPEED = 132;
    public static final double GUARDIAN_DETECT_RANGE = 260;
    public static final double ENEMY_COUNTDOWN_AFTER_HIT = 0.25;

    // ---- Phần thưởng -------------------------------------------------------
    public static final int COIN_VALUE = 5;
    public static final int SHARD_SCORE = 1;
}
