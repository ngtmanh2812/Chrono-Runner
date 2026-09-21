package chronorunner.entity;

import chronorunner.core.Physics;
import chronorunner.core.TimeState;
import chronorunner.entity.enemy.Enemy;
import chronorunner.util.Collision;
import chronorunner.util.Progress;
import chronorunner.util.Rect;
import chronorunner.world.Level;
import chronorunner.world.TileType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Alex - nhà du hành thời gian, nhân vật do người chơi điều khiển.
 *
 * <p>Sở hữu toàn bộ năng lực đặc biệt của trò chơi:</p>
 * <ul>
 *   <li><b>Rewind</b> (Q): tua ngược vị trí, vận tốc, máu và cả thời đại
 *       về trạng thái cách đây vài giây. Dữ liệu được lưu trong một
 *       bộ đệm vòng {@link ArrayDeque} gồm các {@link Snapshot}.</li>
 *   <li><b>Dash</b> (Shift): lướt nhanh, mở khoá qua nâng cấp.</li>
 *   <li><b>Nhảy đôi</b>: mở khoá qua nâng cấp.</li>
 *   <li><b>Đóng băng thời gian</b> (F): dừng kẻ địch và bệ động.</li>
 * </ul>
 */
public class Player extends Character {

    /** Một khung hình trạng thái của người chơi, phục vụ tua ngược. */
    private static final class Snapshot {
        double x;
        double y;
        double velX;
        double velY;
        int health;
        int timeOrdinal;
        boolean onGround;
        boolean facingRight;
        double energy;
    }

    /** Số giây dữ liệu được giữ lại trong bộ đệm (đủ cho cấp nâng cấp cao nhất). */
    private static final int HISTORY_SECONDS = 13;
    private static final int HISTORY_CAPACITY = HISTORY_SECONDS * 60;

    private static final Color COAT = new Color(0x2E6E8E);
    private static final Color COAT_LIGHT = new Color(0x49A6C8);
    private static final Color SKIN = new Color(0xE8B98A);
    private static final Color SCARF = new Color(0xE8574A);

    private final Progress progress;

    private double timeEnergy;
    private double maxTimeEnergy;

    // Nhảy
    private int jumpsUsed;
    private double coyoteTimer;
    private double jumpBufferTimer;
    private boolean jumpWasHeld;

    // Dash
    private boolean dashing;
    private double dashTimer;
    private double dashCooldownTimer;
    private double dashDirection = 1;

    // Tấn công
    private double attackTimer;
    private double attackCooldownTimer;
    private final Rect attackBox = new Rect();
    private boolean attackActive;

    // Tua ngược
    private final ArrayDeque<Snapshot> history = new ArrayDeque<>(HISTORY_CAPACITY);
    private final List<Snapshot> snapshotPool = new ArrayList<>();
    private boolean rewinding;
    private int rewindStepsLeft;
    private double rewindTrailTimer;

    // Hình ảnh
    private double runCycle;
    private double squash;
    private double breath;

    private double spawnX;
    private double spawnY;

    public Player(double x, double y, Progress progress) {
        super(x, y, 22, 34, 5);
        this.progress = progress;
        this.maxTimeEnergy = progress.maxTimeEnergy();
        this.timeEnergy = maxTimeEnergy;
        this.spawnX = x;
        this.spawnY = y;
        this.friction = Physics.PLAYER_GROUND_FRICTION;
    }

    // ---- Truy vấn năng lực -------------------------------------------------
    public double timeEnergy() {
        return timeEnergy;
    }

    public double maxTimeEnergy() {
        return maxTimeEnergy;
    }

    public double energyRatio() {
        return maxTimeEnergy <= 0 ? 0 : timeEnergy / maxTimeEnergy;
    }

    public void addTimeEnergy(double amount) {
        timeEnergy = Collision.clamp(timeEnergy + amount, 0, maxTimeEnergy);
    }

    public double rewindSeconds() {
        return progress.rewindSeconds();
    }

    public boolean isRewinding() {
        return rewinding;
    }

    public boolean isDashing() {
        return dashing;
    }

    public boolean isAttackActive() {
        return attackActive;
    }

    public Rect attackBox() {
        return attackBox;
    }

    /** Nạp lại chỉ số sau khi người chơi mua nâng cấp. */
    public void refreshUpgrades() {
        double ratio = energyRatio();
        maxTimeEnergy = progress.maxTimeEnergy();
        timeEnergy = maxTimeEnergy * ratio;
    }

    public int maxJumps() {
        return progress.hasDoubleJump() ? 2 : 1;
    }

    public void setSpawn(double sx, double sy) {
        this.spawnX = sx;
        this.spawnY = sy;
    }

    public double spawnX() {
        return spawnX;
    }

    public double spawnY() {
        return spawnY;
    }

    // ---- Vòng đời ----------------------------------------------------------
    @Override
    protected void onDeath() {
        velX = 0;
        velY = 0;
    }

    @Override
    public void update(Level level, double dt) {
        age += dt;
        breath += dt;

        if (invulnTimer > 0) {
            invulnTimer -= dt;
        }
        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= dt;
        }
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= dt;
        }
        if (attackTimer > 0) {
            attackTimer -= dt;
            if (attackTimer <= 0) {
                attackActive = false;
            }
        }

        if (rewinding) {
            stepRewind(level, dt);
            return;
        }

        regenerateEnergy(level, dt);
        handleInput(level, dt);
        applyDash(level, dt);
        applyGravity(level, dt);
        applyCarriedMotion();
        moveWithCollision(level, dt);
        updateAttackBox();
        resolveAttack(level);
        resolveStomp(level);
        checkHazards(level);
        updateAnimation(dt);
        recordSnapshot(level);

        if (y > level.worldHeight() + 120) {
            damagePiercing(99, centerX());
        }
    }

    private void regenerateEnergy(Level level, double dt) {
        if (!level.isTimeFrozen()) {
            addTimeEnergy(Physics.ENERGY_REGEN_PER_SECOND * dt);
        }
    }

    // ---- Điều khiển --------------------------------------------------------
    private void handleInput(Level level, double dt) {
        if (!alive) {
            applyFriction(level, dt);
            return;
        }

        double dir = 0;
        if (level.input().left()) {
            dir -= 1;
        }
        if (level.input().right()) {
            dir += 1;
        }

        if (!dashing) {
            if (dir != 0) {
                accelerateHorizontally(dir, Physics.PLAYER_RUN_SPEED, level, dt);
            } else {
                applyFriction(level, dt);
            }
        }

        // ---- Nhảy ----
        if (level.input().jumpPressed()) {
            jumpBufferTimer = Physics.JUMP_BUFFER_TIME;
        } else {
            jumpBufferTimer -= dt;
        }

        if (onGround) {
            jumpsUsed = 0;
            coyoteTimer = Physics.COYOTE_TIME;
        } else {
            coyoteTimer -= dt;
        }

        boolean jumpHeld = level.input().jumpHeld();
        if (jumpBufferTimer > 0) {
            if (coyoteTimer > 0) {
                doJump(Physics.JUMP_VELOCITY);
                jumpsUsed = 1;
            } else if (jumpsUsed < maxJumps()) {
                doJump(Physics.DOUBLE_JUMP_VELOCITY);
                jumpsUsed++;
                // Hiệu ứng vòng xoáy khi nhảy đôi
                for (int i = 0; i < 12; i++) {
                    double a = Math.PI * 2 * i / 12;
                    level.spawn(new Particle(centerX(), bottom(),
                            Math.cos(a) * 90, Math.abs(Math.sin(a)) * 40,
                            new Color(0x9FE8FF), 0.35, 3, false));
                }
            }
        }

        // Nhả phím sớm -> nhảy thấp
        if (jumpWasHeld && !jumpHeld && velY < 0) {
            velY *= Physics.JUMP_CUT_MULTIPLIER;
        }
        jumpWasHeld = jumpHeld;

        // ---- Dash ----
        if (level.input().dashPressed()) {
            tryDash(level, dir);
        }

        // ---- Tấn công ----
        if (level.input().attackPressed() && attackCooldownTimer <= 0) {
            startAttack(level);
        }

        // ---- Tua ngược ----
        if (level.input().rewindPressed()) {
            startRewind(level);
        }

        // ---- Đóng băng thời gian ----
        if (level.input().freezePressed()) {
            tryFreeze(level);
        }
    }

    private void doJump(double velocity) {
        velY = velocity;
        onGround = false;
        jumpBufferTimer = 0;
        coyoteTimer = 0;
        squash = -0.35;
    }

    private void tryDash(Level level, double dir) {
        if (!progress.hasDash() || dashing || dashCooldownTimer > 0) {
            return;
        }
        if (timeEnergy < Physics.DASH_ENERGY_COST) {
            level.game().toast("Không đủ năng lượng để Dash");
            return;
        }
        timeEnergy -= Physics.DASH_ENERGY_COST;
        dashing = true;
        dashTimer = Physics.DASH_DURATION;
        dashCooldownTimer = progress.dashCooldown();
        dashDirection = dir != 0 ? dir : (facingRight ? 1 : -1);
        facingRight = dashDirection > 0;
        setInvulnerable(Physics.DASH_DURATION + 0.05);
        level.camera().shake(3, 0.12);
    }

    private void applyDash(Level level, double dt) {
        if (!dashing) {
            return;
        }
        dashTimer -= dt;
        velX = dashDirection * Physics.DASH_SPEED;
        velY = 0;
        // Vệt mờ phía sau
        level.spawn(new Particle(centerX() - dashDirection * 8, centerY(),
                -dashDirection * 40, 0, new Color(0xA8E9FF), 0.26, 5, false));
        if (dashTimer <= 0) {
            dashing = false;
            velX *= 0.55;
        }
    }

    private void startAttack(Level level) {
        attackCooldownTimer = Physics.ATTACK_COOLDOWN;
        attackTimer = Physics.ATTACK_DURATION;
        attackActive = true;
        updateAttackBox();
        level.camera().shake(2, 0.08);
    }

    private void updateAttackBox() {
        double bx = facingRight ? right() - 2 : left() - Physics.ATTACK_REACH + 2;
        attackBox.set(bx, y + 4, Physics.ATTACK_REACH, height - 8);
    }

    private void resolveAttack(Level level) {
        if (!attackActive) {
            return;
        }
        for (Enemy e : level.enemies()) {
            if (!e.isAlive() || !e.isDamageable() || !e.existsIn(level.timeState())) {
                continue;
            }
            if (attackBox.intersects(e.bounds())) {
                e.takeDamage(Physics.ATTACK_DAMAGE, centerX());
            }
        }
        for (Projectile p : level.projectiles()) {
            if (p.isHostile() && attackBox.intersects(p.bounds())) {
                p.deflect(centerX());
            }
        }
    }

    /** Giẫm lên đầu kẻ địch: gây sát thương và nảy lên. */
    private void resolveStomp(Level level) {
        if (velY <= 0) {
            return;
        }
        for (Enemy e : level.enemies()) {
            if (!e.isAlive() || !e.isStompable() || !e.isDamageable()) {
                continue;
            }
            if (!intersects(e)) {
                continue;
            }
            if (bottom() - e.top() < 14) {
                e.takeDamage(2, centerX());
                velY = Physics.STOMP_BOUNCE;
                jumpsUsed = Math.min(jumpsUsed, 1);
                level.camera().shake(4, 0.14);
                level.spawn(new FloatingText(e.centerX() - 8, e.top() - 6, "GIẪM!", Color.WHITE, 0.5));
            }
        }
    }

    // ---- Bẫy ---------------------------------------------------------------
    private void checkHazards(Level level) {
        TileType hazard = level.hazardAt(x + 4, y + 4, width - 8, height - 6);
        if (hazard == null || !alive) {
            return;
        }
        if (hazard == TileType.LAVA) {
            if (invulnTimer <= 0) {
                damage(1, centerX());
                velY = -520;
                velX = -Collision.sign(velX) * 140;
                level.camera().shake(8, 0.3);
            }
        } else if (invulnTimer <= 0) {
            damage(1, centerX());
            level.camera().shake(6, 0.25);
        }
    }

    private void updateAnimation(double dt) {
        if (Math.abs(velX) > 12 && onGround) {
            runCycle += dt * Math.abs(velX) * 0.045;
        } else {
            runCycle += dt * 2;
        }
        squash = Collision.approach(squash, 0, dt * 3.2);
    }

    // ---- Tua ngược thời gian ----------------------------------------------
    private void recordSnapshot(Level level) {
        Snapshot s = obtainSnapshot();
        s.x = x;
        s.y = y;
        s.velX = velX;
        s.velY = velY;
        s.health = health;
        s.timeOrdinal = level.timeState().ordinal();
        s.onGround = onGround;
        s.facingRight = facingRight;
        s.energy = timeEnergy;

        history.addLast(s);
        while (history.size() > HISTORY_CAPACITY) {
            snapshotPool.add(history.pollFirst());
        }
    }

    private Snapshot obtainSnapshot() {
        if (snapshotPool.isEmpty()) {
            return new Snapshot();
        }
        return snapshotPool.remove(snapshotPool.size() - 1);
    }

    private void startRewind(Level level) {
        if (rewinding || history.size() < 30) {
            return;
        }
        if (timeEnergy < Physics.REWIND_ENERGY_COST) {
            level.game().toast("Không đủ năng lượng để tua ngược");
            return;
        }

        timeEnergy -= Physics.REWIND_ENERGY_COST;
        int requested = Collision.secondsToTicks(rewindSeconds());
        rewindStepsLeft = Math.min(requested, history.size() - 1);
        rewinding = true;
        setInvulnerable(rewindSeconds() + 0.5);
        level.camera().shake(9, 0.35);
        level.game().toast("TUA NGƯỢC " + String.format("%.1f", rewindSeconds()) + "s");
    }

    private void stepRewind(Level level, double dt) {
        rewindTrailTimer -= dt;
        if (rewindTrailTimer <= 0) {
            rewindTrailTimer = 0.02;
            level.spawn(new Particle(centerX(), centerY(),
                    (Math.random() - 0.5) * 60, (Math.random() - 0.5) * 60,
                    new Color(0x7FE9FF), 0.4, 6, false));
        }

        if (rewindStepsLeft <= 0) {
            rewinding = false;
            return;
        }

        // Càng về cuối càng "cuộn" nhanh để tổng thời gian tua không quá dài.
        int steps = (int) Math.max(1, Math.round(Physics.REWIND_PLAYBACK_SPEED));
        for (int i = 0; i < steps && rewindStepsLeft > 0; i++) {
            Snapshot s = history.pollLast();
            if (s == null) {
                break;
            }
            applySnapshot(level, s);
            snapshotPool.add(s);
            rewindStepsLeft--;
        }
    }

    private void applySnapshot(Level level, Snapshot s) {
        x = s.x;
        y = s.y;
        velX = s.velX;
        velY = s.velY;
        facingRight = s.facingRight;
        onGround = s.onGround;
        health = Collision.clampInt(s.health, 1, maxHealth);
        timeEnergy = Math.max(timeEnergy, s.energy);

        TimeState past = TimeState.ofOrdinal(s.timeOrdinal);
        if (past != level.timeState()) {
            level.shiftTime(past);
        }
    }

    // ---- Đóng băng thời gian ----------------------------------------------
    private void tryFreeze(Level level) {
        if (!progress.hasTimeFreeze()) {
            level.game().toast("Chưa mở khoá Đóng Băng Thời Gian");
            return;
        }
        if (level.isTimeFrozen()) {
            return;
        }
        if (timeEnergy < Physics.FREEZE_ENERGY_COST) {
            level.game().toast("Không đủ năng lượng để đóng băng");
            return;
        }
        timeEnergy -= Physics.FREEZE_ENERGY_COST;
        level.freezeTime(progress.freezeSeconds());
        level.game().toast("ĐÓNG BĂNG THỜI GIAN");
    }

    // ---- Va chạm với kẻ địch ----------------------------------------------
    /** Được {@link Enemy} gọi khi kẻ địch chạm vào người chơi. */
    public void onEnemyContact(Enemy enemy, int damage) {
        if (rewinding) {
            return;
        }
        damage(damage, enemy.centerX());
    }

    // ---- Vẽ ----------------------------------------------------------------
    @Override
    public void render(Graphics2D g) {
        renderBody(g, null);
    }

    /** Vẽ nhân vật; cần {@code level} để vẽ bóng đổ xuống mặt đất. */
    public void render(Graphics2D g, Level level) {
        renderBody(g, level);
    }

    private void renderBody(Graphics2D g, Level level) {
        if (level != null) {
            renderShadow(g, level);
        }

        // Nhấp nháy khi bất tử
        if (invulnTimer > 0 && !rewinding && ((int) (invulnTimer * 20)) % 2 == 0) {
            return;
        }

        int cx = (int) Math.round(centerX());
        double sq = squash;
        int bodyW = (int) Math.round(width * (1 - sq * 0.35));
        int bodyH = (int) Math.round(height * (1 + sq * 0.35));
        int bx = cx - bodyW / 2;
        int by = (int) Math.round(bottom()) - bodyH;


        // ---- Chân ----
        g.setColor(new Color(0x1E2A36));
        if (onGround && Math.abs(velX) > 12) {
            double swing = Math.sin(runCycle) * 6;
            g.fillRect(cx - 7, by + bodyH - 8, 6, (int) (8 + swing));
            g.fillRect(cx + 1, by + bodyH - 8, 6, (int) (8 - swing));
        } else if (!onGround) {
            g.fillRect(cx - 8, by + bodyH - 9, 6, 8);
            g.fillRect(cx + 2, by + bodyH - 6, 6, 8);
        } else {
            g.fillRect(cx - 7, by + bodyH - 7, 6, 7);
            g.fillRect(cx + 1, by + bodyH - 7, 6, 7);
        }

        // ---- Thân ----
        g.setColor(rewinding ? new Color(0x2E8FA8) : COAT);
        g.fillRoundRect(bx, by + 10, bodyW, bodyH - 14, 7, 7);
        g.setColor(COAT_LIGHT);
        g.fillRoundRect(bx + 3, by + 12, bodyW - 6, 6, 4, 4);

        // Ánh sáng Chrono Core ở ngực
        int coreAlpha = (int) (150 + 90 * Math.sin(breath * 4));
        g.setColor(new Color(127, 233, 255, coreAlpha));
        g.fillOval(cx - 3, by + 17, 6, 6);

        // ---- Đầu ----
        int headSize = 15;
        int hx = cx - headSize / 2;
        int hy = by - 1;
        g.setColor(SKIN);
        g.fillRoundRect(hx, hy, headSize, headSize, 6, 6);

        // Kính bảo hộ
        g.setColor(new Color(0x22303C));
        g.fillRect(hx, hy + 5, headSize, 4);
        g.setColor(new Color(0x7FE9FF));
        g.fillRect(hx + (facingRight ? 7 : 3), hy + 5, 5, 4);

        // ---- Khăn quàng bay theo hướng chạy ----
        int scarfDir = facingRight ? -1 : 1;
        double flutter = Math.sin(breath * 7) * 3;
        g.setColor(SCARF);
        g.fillPolygon(new Polygon(
                new int[]{cx, cx + scarfDir * 14, cx + scarfDir * 20},
                new int[]{by + 13, by + 10 + (int) flutter, by + 20 + (int) flutter},
                3));

        // ---- Vệt dash ----
        if (dashing) {
            g.setColor(new Color(168, 233, 255, 150));
            for (int i = 1; i <= 3; i++) {
                g.fillRoundRect(bx - (int) (dashDirection * i * 11), by + 12 + i * 3, 6, bodyH - 20, 3, 3);
            }
        }

        // ---- Vệt chém ----
        if (attackActive) {
            int dir = facingRight ? 1 : -1;
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, 210));
            int arcX = cx + dir * 10;
            g.drawArc(arcX - 22, by + 4, 44, bodyH - 8, facingRight ? -70 : 110, 140);
            g.setStroke(old);
        }

        // ---- Vòng hào quang khi tua ngược ----
        if (rewinding) {
            g.setColor(new Color(127, 233, 255, 90));
            int r = 26 + (int) (4 * Math.sin(age * 20));
            g.drawOval(cx - r, (int) (centerY()) - r, r * 2, r * 2);
        }
    }
}
