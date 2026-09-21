package chronorunner.entity.boss;

import chronorunner.core.Physics;
import chronorunner.entity.Particle;
import chronorunner.entity.Player;
import chronorunner.entity.enemy.Enemy;
import chronorunner.util.Collision;
import chronorunner.world.Level;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Paradox - trùm cuối, phiên bản tương lai của chính Alex.
 *
 * <p>Trận đánh gồm ba phase:</p>
 * <ol>
 *   <li><b>Phase 1</b> - cận chiến: đuổi theo và dịch chuyển tức thời.</li>
 *   <li><b>Phase 2</b> - tạo bản sao: trùm được bảo vệ cho tới khi
 *       tất cả {@link ParadoxClone} bị tiêu diệt.</li>
 *   <li><b>Phase 3</b> - thao túng thời gian: đóng băng, mưa đạn,
 *       tạo rồi xoá nền, tự hồi máu.</li>
 * </ol>
 */
public class TheParadox extends Enemy {

    private static final Color BODY = new Color(0x241030);
    private static final Color CRACK = new Color(0xFF5FD2);
    private static final Color AURA = new Color(0xC08CF0);

    private static final int PHASE_TWO_HEALTH = 20;
    private static final int PHASE_THREE_HEALTH = 10;

    private enum Special {
        FREEZE, ORB_RAIN, PLATFORM_ERASE, SELF_REWIND
    }

    private int phase = 1;

    private final List<ParadoxClone> clones = new ArrayList<>();
    private final List<ParadoxBlock> spawnedBlocks = new ArrayList<>();

    private boolean shieldActive;

    private double blinkCooldown = 3.0;
    private double shootCooldown = 1.5;
    private double specialCooldown = 3.0;
    private double blockEraseTimer = -1;
    private double frozenTimer;
    private double auraPhase;
    private int specialIndex;
    private int facingDir = -1;

    /** Giới hạn đấu trường: trùm chỉ thức giấc khi người chơi bước vào. */
    private double arenaLeft = Double.NEGATIVE_INFINITY;
    private double arenaRight = Double.POSITIVE_INFINITY;
    private boolean awakened;

    public TheParadox(double x, double y) {
        super(x, y, 46, 64, 30);
        this.contactDamage = 1;
        this.coinReward = 120;
        this.stompable = false;
        this.friction = 2600;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public String displayName() {
        return "THE PARADOX";
    }

    public int phase() {
        return phase;
    }

    public boolean isShielded() {
        return shieldActive;
    }

    public int aliveClones() {
        return clones.size();
    }

    /**
     * Giới hạn trùm trong đấu trường cuối màn.
     *
     * <p>Nhờ vậy trùm không đuổi theo người chơi ra khỏi khu vực quy định;
     * trận đánh chỉ bắt đầu khi người chơi thực sự bước vào.</p>
     */
    public void setArena(double leftX, double rightX) {
        this.arenaLeft = leftX;
        this.arenaRight = rightX;
    }

    public boolean isAwakened() {
        return awakened;
    }

    // ---- Vòng cập nhật -----------------------------------------------------
    @Override
    protected void think(Level level, double dt) {
        auraPhase += dt;

        if (!awakened) {
            // Chưa tới đấu trường: trùm đứng yên chờ, không đuổi theo người chơi.
            Player target = level.player();
            if (target == null || !target.isAlive()
                    || target.centerX() < arenaLeft || target.centerX() > arenaRight) {
                applyGravity(level, dt);
                moveWithCollision(level, dt);
                checkOutOfBounds(level);
                return;
            }
            awakened = true;
            level.game().toast("THE PARADOX: \"Ta chính là ngươi... của ngày sau.\"");
            level.camera().shake(16, 1.0);
            Particle.burst(level, centerX(), centerY(), 40, 300, CRACK, 1.0);
        }

        applyGravity(level, dt);
        updatePhase(level);

        if (blinkCooldown > 0) {
            blinkCooldown -= dt;
        }
        if (shootCooldown > 0) {
            shootCooldown -= dt;
        }
        if (specialCooldown > 0) {
            specialCooldown -= dt;
        }
        if (blockEraseTimer > 0) {
            blockEraseTimer -= dt;
            if (blockEraseTimer <= 0) {
                for (ParadoxBlock b : spawnedBlocks) {
                    b.dissolve(level);
                }
                spawnedBlocks.clear();
                level.game().toast("The Paradox xoá nền!");
            }
        }

        switch (phase) {
            case 1:
                phaseOne(level, dt);
                break;
            case 2:
                phaseTwo(level, dt);
                break;
            default:
                phaseThree(level, dt);
                break;
        }

        moveWithCollision(level, dt);
        faceTowards(centerX() + facingDir * 10);
        cleanup();
        checkOutOfBounds(level);
        clampToArena(level);
    }

    /** Không cho trùm rời khỏi đấu trường dù có dịch chuyển tức thời. */
    private void clampToArena(Level level) {
        if (arenaLeft == Double.NEGATIVE_INFINITY && arenaRight == Double.POSITIVE_INFINITY) {
            return;
        }
        double minX = Math.max(0, arenaLeft);
        double maxX = Math.min(level.worldWidth() - width, arenaRight - width);
        if (maxX < minX) {
            return;
        }
        double clamped = Collision.clamp(x, minX, maxX);
        if (clamped != x) {
            x = clamped;
            velX = 0;
        }
    }

    private void updatePhase(Level level) {
        if (phase == 1 && health <= PHASE_TWO_HEALTH) {
            enterPhaseTwo(level);
        } else if (phase == 2 && health <= PHASE_THREE_HEALTH) {
            enterPhaseThree(level);
        }
    }

    private void enterPhaseTwo(Level level) {
        phase = 2;
        shieldActive = true;
        damageable = false;
        specialCooldown = 2.6;

        for (int i = 0; i < 2; i++) {
            double ox = (i == 0 ? -90 : 90);
            ParadoxClone clone = new ParadoxClone(centerX() + ox, y - 20, this);
            clones.add(clone);
            level.spawn(clone);
            Particle.burst(level, clone.centerX(), clone.centerY(), 18, 180, CRACK, 0.6);
        }
        level.game().toast("THE PARADOX tạo bản sao - hãy phá hết chúng!");
        level.camera().shake(12, 0.6);
    }

    private void enterPhaseThree(Level level) {
        phase = 3;
        shieldActive = false;
        damageable = true;
        specialCooldown = 1.8;
        level.game().toast("THE PARADOX thao túng thời gian!");
        level.camera().shake(14, 0.8);
        Particle.burst(level, centerX(), centerY(), 40, 260, AURA, 0.9);
    }

    // ---- Hành vi từng phase ------------------------------------------------
    private void phaseOne(Level level, double dt) {
        Player p = level.player();
        if (p == null || !p.isAlive()) {
            return;
        }
        facingDir = p.centerX() > centerX() ? 1 : -1;
        velX = Collision.approach(velX, facingDir * 118, 700 * dt);

        if (onGround && p.centerY() < centerY() - 60 && Math.random() < 0.02) {
            velY = Physics.JUMP_VELOCITY * 0.9;
        }

        if (blinkCooldown <= 0) {
            blinkNear(level, p, 90);
            blinkCooldown = 4.2;
        }
    }

    private void phaseTwo(Level level, double dt) {
        Player p = level.player();
        if (p == null || !p.isAlive()) {
            return;
        }

        // Giữ khoảng cách và bắn
        double dist = p.centerX() - centerX();
        facingDir = dist > 0 ? 1 : -1;
        if (Math.abs(dist) < 170) {
            velX = Collision.approach(velX, -facingDir * 130, 800 * dt);
        } else if (Math.abs(dist) > 320) {
            velX = Collision.approach(velX, facingDir * 110, 800 * dt);
        } else {
            velX = Collision.approach(velX, 0, 900 * dt);
        }

        if (shootCooldown <= 0) {
            shootSpread(level, p, 3, 190);
            shootCooldown = 1.7;
        }
        if (blinkCooldown <= 0) {
            blinkNear(level, p, 200);
            blinkCooldown = 5.0;
        }
    }

    private void phaseThree(Level level, double dt) {
        Player p = level.player();
        if (p == null || !p.isAlive()) {
            return;
        }
        facingDir = p.centerX() > centerX() ? 1 : -1;
        velX = Collision.approach(velX, facingDir * 150, 900 * dt);

        if (specialCooldown <= 0) {
            Special s = Special.values()[specialIndex % Special.values().length];
            specialIndex++;
            castSpecial(level, p, s);
            specialCooldown = 3.2;
        }
    }

    private void castSpecial(Level level, Player p, Special s) {
        switch (s) {
            case FREEZE:
                level.freezeTime(2.4);
                level.game().toast("The Paradox đóng băng thời gian!");
                break;
            case ORB_RAIN:
                for (int i = 0; i < 5; i++) {
                    double sx = p.centerX() - 160 + i * 80;
                    level.spawn(new ParadoxOrb(sx, p.centerY() - 320, 0, 1, 230, 1));
                }
                level.game().toast("Mưa nghịch lý!");
                break;
            case PLATFORM_ERASE:
                for (int i = -1; i <= 1; i++) {
                    ParadoxBlock b = new ParadoxBlock(
                            p.centerX() + i * 40 - 18, p.bottom() + 10, 36, 18, 3.0);
                    spawnedBlocks.add(b);
                    level.spawn(b);
                }
                blockEraseTimer = 1.6;
                level.game().toast("Nền tạm thời được tạo ra...");
                break;
            case SELF_REWIND:
                heal(3);
                blinkNear(level, p, 260);
                level.game().toast("The Paradox tua ngược vết thương!");
                Particle.burst(level, centerX(), centerY(), 24, 200, AURA, 0.7);
                break;
            default:
        }
    }

    private void shootSpread(Level level, Player target, int count, double speed) {
        double baseDx = target.centerX() - centerX();
        double baseDy = target.centerY() - centerY();
        double baseAngle = Math.atan2(baseDy, baseDx);
        double spread = 0.28;
        for (int i = 0; i < count; i++) {
            double a = baseAngle + (i - (count - 1) / 2.0) * spread;
            level.spawn(new ParadoxOrb(centerX() - 7, centerY() - 7,
                    Math.cos(a), Math.sin(a), speed, 1));
        }
    }

    private void blinkNear(Level level, Player target, double offset) {
        Particle.burst(level, centerX(), centerY(), 16, 160, CRACK, 0.45);
        double minX = arenaLeft == Double.NEGATIVE_INFINITY ? 40 : Math.max(40, arenaLeft);
        double maxX = arenaRight == Double.POSITIVE_INFINITY
                ? level.worldWidth() - width - 40
                : Math.min(level.worldWidth() - width - 40, arenaRight - width - 40);
        if (maxX < minX) {
            maxX = minX;
        }
        double nx = Collision.clamp(target.centerX() + (Math.random() < 0.5 ? -offset : offset),
                minX, maxX);
        setPosition(nx - width / 2, target.getY() - 20);
        Particle.burst(level, centerX(), centerY(), 16, 160, CRACK, 0.45);
        level.camera().shake(5, 0.2);
    }

    // ---- Bảo vệ & phá giải đóng băng ---------------------------------------
    @Override
    public void takeDamage(int amount, double sourceX) {
        if (shieldActive) {
            // Khiên bản sao đang che chở: đòn đánh bật ra mà không mất máu.
            hitFlash = 0.12;
            return;
        }
        super.takeDamage(amount, sourceX);
    }

    @Override
    protected void applyKnockback(double sourceX) {
        // Trùm nặng nên gần như không bị đẩy lùi.
        velX = Collision.sign(velX) * 30;
    }

    @Override
    protected void onFrozenTick(Level level, double dt) {
        frozenTimer += dt;
        double limit = phase >= 2 ? 1.4 : 3.0;
        if (frozenTimer >= limit) {
            frozenTimer = 0;
            level.clearFreeze();
            level.game().toast("The Paradox phá giải đóng băng!");
            level.camera().shake(8, 0.3);
        }
    }

    /** Được {@link ParadoxClone} gọi ngay khi bị tiêu diệt. */
    public void onCloneDefeated() {
        for (ParadoxClone c : clones) {
            if (c.isAlive()) {
                return; // vẫn còn bản sao -> khiên còn hiệu lực
            }
        }
        shieldActive = false;
        damageable = true;
    }

    private void cleanup() {
        for (Iterator<ParadoxClone> it = clones.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }
        for (Iterator<ParadoxBlock> it = spawnedBlocks.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }

        // Dự phòng: bản sao chết vì rơi khỏi màn chơi.
        if (shieldActive && clones.isEmpty() && phase == 2) {
            shieldActive = false;
            damageable = true;
        }
    }

    @Override
    protected void onDeath() {
        super.onDeath();
        shieldActive = false;
        for (ParadoxClone c : clones) {
            c.kill();
        }
        clones.clear();
    }

    // ---- Vẽ ----------------------------------------------------------------
    @Override
    public void render(Graphics2D g) {
        int cx = (int) Math.round(centerX());
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rw = (int) Math.round(width);
        int rh = (int) Math.round(height);

        // Hào quang thời gian
        int auraAlpha = (int) (40 + 30 * Math.sin(auraPhase * 3));
        g.setColor(new Color(AURA.getRed(), AURA.getGreen(), AURA.getBlue(), auraAlpha));
        g.fillOval(cx - rw, ry - 20, rw * 2, rh + 40);

        // Vòng đồng hồ xoay quanh
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(CRACK.getRed(), CRACK.getGreen(), CRACK.getBlue(), 170));
        for (int i = 0; i < 2; i++) {
            double a = auraPhase * (1.2 + i * 0.6);
            int w = rw + 24 + i * 18;
            int h = rh - 10 + i * 12;
            g.drawOval(cx - w / 2 + (int) (Math.cos(a) * 8), ry + 6 - h / 2 + rh / 2 + (int) (Math.sin(a) * 8), w, h);
        }
        g.setStroke(old);

        // Áo choàng năng lượng
        int capeDir = facingDir > 0 ? -1 : 1;
        g.setColor(new Color(0x3A1150));
        g.fillPolygon(new Polygon(
                new int[]{cx, cx + capeDir * 34, cx + capeDir * 18},
                new int[]{ry + 16, ry + rh + 12, ry + rh - 4}, 3));

        // Chân
        g.setColor(new Color(0x140A1C));
        g.fillRect(rx + 7, ry + rh - 18, 12, 18);
        g.fillRect(rx + rw - 19, ry + rh - 18, 12, 18);

        // Thân
        g.setColor(BODY);
        g.fillRoundRect(rx, ry + 16, rw, rh - 30, 10, 10);

        // Vết nứt phát sáng
        g.setColor(new Color(CRACK.getRed(), CRACK.getGreen(), CRACK.getBlue(),
                (int) (150 + 90 * Math.sin(auraPhase * 5))));
        g.drawLine(rx + 10, ry + 24, rx + rw - 8, ry + 44);
        g.drawLine(rx + rw - 10, ry + 22, rx + 12, ry + 46);
        g.drawLine(cx, ry + 20, cx, ry + rh - 18);

        // Đầu
        g.setColor(new Color(0x1A0B24));
        g.fillRoundRect(rx + 7, ry - 4, rw - 14, 22, 8, 8);
        g.setColor(CRACK);
        g.fillRect(rx + 12, ry + 5, 9, 4);
        g.fillRect(rx + rw - 21, ry + 5, 9, 4);

        // Khiên bảo vệ ở phase 2
        if (shieldActive) {
            g.setColor(new Color(192, 140, 240, 70));
            g.fillOval(cx - rw, ry - 24, rw * 2, rh + 48);
            g.setColor(new Color(255, 255, 255, 190));
            g.drawOval(cx - rw, ry - 24, rw * 2, rh + 48);
        }

        if (hitFlash > 0) {
            g.setColor(new Color(255, 255, 255, (int) (170 * (hitFlash / 0.16))));
            g.fillRoundRect(rx - 3, ry - 6, rw + 6, rh + 10, 12, 12);
        }
    }
}
