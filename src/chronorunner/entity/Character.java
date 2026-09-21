package chronorunner.entity;

import chronorunner.core.Physics;
import chronorunner.obstacle.Platform;
import chronorunner.util.Collision;
import chronorunner.world.Level;

/**
 * Lớp cơ sở cho mọi thực thể "sống": người chơi, kẻ địch, trùm.
 *
 * <p>Cung cấp sẵn:</p>
 * <ul>
 *   <li>máu, bất tử tạm thời sau khi trúng đòn,</li>
 *   <li>trọng lực,</li>
 *   <li>di chuyển có xử lý va chạm theo từng trục (AABB swept đơn giản),</li>
 *   <li>tương tác với {@link Platform} động (bệ di chuyển, bệ rơi...).</li>
 * </ul>
 */
public abstract class Character extends GameObject {

    protected int health;
    protected int maxHealth;

    protected double invulnTimer;
    protected boolean onGround;
    protected boolean facingRight = true;
    protected double gravityScale = 1.0;
    protected double friction = Physics.PLAYER_GROUND_FRICTION;

    /** Bệ động mà nhân vật đang đứng lên (nếu có) - dùng để "dính" theo bệ. */
    protected Platform standingPlatform;
    /** Quãng đường bệ đã đi trong bước trước, giúp nhân vật di chuyển theo. */
    protected double carriedDx;
    protected double carriedDy;

    /** Thời điểm chạm đất gần nhất, phục vụ coyote time. */
    protected double airTime;

    protected Character(double x, double y, double w, double h, int maxHealth) {
        super(x, y, w, h);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    // ---- Máu ---------------------------------------------------------------
    public int health() {
        return health;
    }

    public int maxHealth() {
        return maxHealth;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void heal(int amount) {
        health = Collision.clampInt(health + amount, 0, maxHealth);
    }

    public void setHealth(int value) {
        health = Collision.clampInt(value, 0, maxHealth);
    }

    public double healthRatio() {
        return maxHealth <= 0 ? 0 : (double) health / maxHealth;
    }

    public boolean isInvulnerable() {
        return invulnTimer > 0;
    }

    /** Hồi sinh tại vị trí mới với đầy máu và bất tử tạm thời. */
    public void reviveAt(double nx, double ny) {
        setPosition(nx, ny);
        velX = 0;
        velY = 0;
        health = maxHealth;
        alive = true;
        onGround = false;
        standingPlatform = null;
        invulnTimer = Physics.INVULN_DURATION * 1.8;
        onRevive();
    }

    /** Hook cho lớp con khi được hồi sinh. */
    protected void onRevive() {
    }

    public void setInvulnerable(double seconds) {
        invulnTimer = Math.max(invulnTimer, seconds);
    }

    /**
     * Gây sát thương kèm đẩy lùi.
     *
     * @param amount  lượng máu mất
     * @param sourceX toạ độ X của nguồn sát thương (để xác định hướng đẩy)
     */
    public void damage(int amount, double sourceX) {
        if (amount <= 0 || invulnTimer > 0 || !alive) {
            return;
        }
        health -= amount;
        invulnTimer = Physics.INVULN_DURATION;
        applyKnockback(sourceX);
        onDamaged(amount, sourceX);
        if (health <= 0) {
            health = 0;
            alive = false;
            onDeath();
        }
    }

    /**
     * Nhận sát thương từ một nguồn. Mặc định chuyển tiếp sang {@link #damage};
     * kẻ địch ghi đè để thêm thời gian bất tử ngắn và hiệu ứng trúng đòn.
     */
    public void takeDamage(int amount, double sourceX) {
        damage(amount, sourceX);
    }

    /** Sát thương bỏ qua bất tử (dùng cho bẫy, dung nham). */
    public void damagePiercing(int amount, double sourceX) {
        if (amount <= 0 || !alive) {
            return;
        }
        invulnTimer = 0;
        damage(amount, sourceX);
    }

    protected void applyKnockback(double sourceX) {
        int dir = centerX() < sourceX ? -1 : 1;
        velX = dir * Physics.KNOCKBACK_X;
        velY = Physics.KNOCKBACK_Y;
    }

    /** Hook cho lớp con khi bị đánh trúng. */
    protected void onDamaged(int amount, double sourceX) {
    }

    /** Hook cho lớp con khi hết máu. */
    protected void onDeath() {
    }

    // ---- Trạng thái --------------------------------------------------------
    public boolean isOnGround() {
        return onGround;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void faceTowards(double targetX) {
        if (Math.abs(targetX - centerX()) > 1) {
            facingRight = targetX > centerX();
        }
    }

    public Platform standingPlatform() {
        return standingPlatform;
    }

    // ---- Vật lý ------------------------------------------------------------
    protected void applyGravity(Level level, double dt) {
        velY += Physics.GRAVITY * gravityScale * dt;
        if (velY > Physics.MAX_FALL_SPEED) {
            velY = Physics.MAX_FALL_SPEED;
        }
    }

    /**
     * Di chuyển theo vận tốc hiện tại và xử lý va chạm với ô bản đồ
     * cùng các bệ động. Đây là trái tim của hệ vật lý.
     */
    protected void moveWithCollision(Level level, double dt) {
        double prevBottom = y + height;

        // ---- Trục X ----
        x += velX * dt;
        collideTilesHorizontal(level);
        collidePlatformsHorizontal(level);

        // ---- Trục Y ----
        y += velY * dt;
        boolean wasOnGround = onGround;
        onGround = false;
        standingPlatform = null;
        collideTilesVertical(level);
        collidePlatformsVertical(level, prevBottom);

        if (onGround) {
            airTime = 0;
        } else {
            airTime += dt;
            if (wasOnGround) {
                // vừa rời mép: vẫn cho phép nhảy trong một khoảng ngắn
                airTime = Math.min(airTime, Physics.COYOTE_TIME * 0.99);
            }
        }
    }

    private void collideTilesHorizontal(Level level) {
        if (!level.solidInRect(x, y, width, height)) {
            return;
        }
        int ts = level.tileSize();
        if (velX > 0) {
            x = Math.floor((x + width) / ts) * ts - width - 0.01;
        } else if (velX < 0) {
            x = Math.ceil(x / ts) * ts + 0.01;
        } else {
            // Bị đẩy do bệ động: thoát ra theo hướng ít tốn kém nhất
            x = Math.ceil(x / ts) * ts + 0.01;
        }
        velX = 0;
    }

    private void collideTilesVertical(Level level) {
        if (!level.solidInRect(x, y, width, height)) {
            return;
        }
        int ts = level.tileSize();
        if (velY > 0) {
            y = Math.floor((y + height) / ts) * ts - height - 0.01;
            onGround = true;
        } else if (velY < 0) {
            y = Math.ceil(y / ts) * ts + 0.01;
        }
        velY = 0;
    }

    private void collidePlatformsHorizontal(Level level) {
        for (Platform p : level.platforms()) {
            if (!p.blocks(this) || !p.intersects(this)) {
                continue;
            }
            if (velX > 0) {
                x = p.left() - width - 0.01;
            } else if (velX < 0) {
                x = p.right() + 0.01;
            }
            velX = 0;
        }
    }

    private void collidePlatformsVertical(Level level, double prevBottom) {
        for (Platform p : level.platforms()) {
            if (!p.blocks(this) || !p.intersects(this)) {
                continue;
            }
            if (velY >= 0) {
                // Bệ một chiều chỉ chặn khi chân ở phía trên mặt bệ trước khi rơi.
                if (p.isOneWay() && prevBottom > p.top() + 4) {
                    continue;
                }
                y = p.top() - height - 0.01;
                velY = 0;
                onGround = true;
                standingPlatform = p;
                p.onStand(this);
            } else {
                if (p.isOneWay()) {
                    continue;
                }
                y = p.bottom() + 0.01;
                velY = 0;
            }
        }
    }

    /** Di chuyển theo bệ đang đứng (gọi ở đầu bước cập nhật). */
    protected void applyCarriedMotion() {
        if (standingPlatform != null && standingPlatform.isAlive()) {
            carriedDx = standingPlatform.deltaX();
            carriedDy = standingPlatform.deltaY();
            x += carriedDx;
            y += carriedDy;
        } else {
            carriedDx = 0;
            carriedDy = 0;
            standingPlatform = null;
        }
    }

    /** Ma sát ngang khi không giữ phím di chuyển. */
    protected void applyFriction(Level level, double dt) {
        double f = level.isOnIce(x, y, width, height + 2) ? Physics.PLAYER_ICE_FRICTION : friction;
        if (Math.abs(velX) <= f * dt) {
            velX = 0;
        } else {
            velX -= Collision.sign(velX) * f * dt;
        }
    }

    /** Tăng tốc ngang về phía mong muốn. */
    protected void accelerateHorizontally(double dir, double targetSpeed, Level level, double dt) {
        double accel = onGround ? Physics.PLAYER_GROUND_ACCEL : Physics.PLAYER_AIR_ACCEL;
        double target = dir * targetSpeed;
        velX = Collision.approach(velX, target, accel * dt);
        if (dir != 0) {
            facingRight = dir > 0;
        }
    }

    protected boolean canCoyoteJump() {
        return onGround || airTime <= Physics.COYOTE_TIME;
    }

    public double airTime() {
        return airTime;
    }

    /** Vẽ bóng đổ mờ dưới chân nhân vật (tăng cảm giác chiều sâu). */
    protected void renderShadow(java.awt.Graphics2D g, Level level) {
        double groundY = y + height;
        int ts = level.tileSize();
        int ty = (int) ((groundY + 2) / ts);
        int tx = (int) (centerX() / ts);
        while (ty < level.heightTiles() && !level.tileAt(tx, ty).solidAnyTime()) {
            ty++;
        }
        if (ty >= level.heightTiles()) {
            return;
        }
        double gy = ty * (double) ts;
        double dist = Math.min(1.0, (gy - groundY) / 240.0);
        int alpha = (int) (90 * (1 - dist));
        if (alpha <= 4) {
            return;
        }
        g.setColor(new java.awt.Color(0, 0, 0, alpha));
        int w = (int) (width * (1 - dist * 0.4));
        g.fillOval((int) (centerX() - w / 2.0), (int) gy - 5, w, 8);
    }
}
