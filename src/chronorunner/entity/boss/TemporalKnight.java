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
import java.util.ArrayDeque;

/**
 * Temporal Knight - mini-boss của màn 4.
 *
 * <p>Điểm đặc biệt: khi bị đánh trúng, hắn <b>tua ngược vị trí của chính mình</b>
 * về chỗ đứng cách đây vài giây và hồi lại một phần máu. Người chơi phải
 * dồn sát thương nhanh hơn tốc độ hồi phục của hắn, hoặc lợi dụng lúc hắn
 * đang tua ngược (không thể phản đòn).</p>
 */
public class TemporalKnight extends Enemy {

    private static final Color PLATE = new Color(0x2B3550);
    private static final Color PLATE_LIGHT = new Color(0x4A5C86);
    private static final Color VISOR = new Color(0x7FE9FF);
    private static final Color CAPE = new Color(0x6A2E8A);

    private static final double REWIND_SECONDS = 2.4;
    private static final double REWIND_COOLDOWN = 7.0;
    private static final int SAMPLE_EVERY = 4;

    private enum State {
        PATROL, CHASE, LUNGE, REWINDING
    }

    private final ArrayDeque<double[]> history = new ArrayDeque<>();

    private State state = State.PATROL;
    private int direction = -1;
    private int tickCounter;
    private double stuckTimer;
    private double lungeCooldown;
    private double lungeTimer;
    private double rewindCooldown;
    private double rewindTimer;
    private double rewindHealPending;

    public TemporalKnight(double x, double y) {
        super(x, y, 34, 46, 10);
        this.contactDamage = 1;
        this.coinReward = 35;
        this.friction = 2200;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public String displayName() {
        return "TEMPORAL KNIGHT";
    }

    @Override
    protected void think(Level level, double dt) {
        applyGravity(level, dt);
        recordHistory();

        if (rewindCooldown > 0) {
            rewindCooldown -= dt;
        }
        if (lungeCooldown > 0) {
            lungeCooldown -= dt;
        }

        switch (state) {
            case REWINDING:
                stepRewind(level, dt);
                return;
            case LUNGE:
                lungeTimer -= dt;
                velX = direction * 400;
                if (lungeTimer <= 0) {
                    state = State.CHASE;
                }
                break;
            default:
                updateApproach(level, dt);
        }

        moveWithCollision(level, dt);
        faceTowards(centerX() + direction * 10);
        preventFalling(level, dt);
        checkOutOfBounds(level);
    }

    private void updateApproach(Level level, double dt) {
        Player p = level.player();
        boolean seesPlayer = p != null && p.isAlive() && distanceTo(p) < 430;

        if (seesPlayer) {
            state = State.CHASE;
            direction = p.centerX() > centerX() ? 1 : -1;
            velX = direction * Physics.GUARDIAN_CHASE_SPEED * 0.9;

            if (lungeCooldown <= 0 && Math.abs(p.centerX() - centerX()) < 210
                    && Math.abs(p.centerY() - centerY()) < 70) {
                state = State.LUNGE;
                lungeTimer = 0.38;
                lungeCooldown = 2.6;
                setInvulnerable(0.15);
            }
        } else {
            state = State.PATROL;
            velX = direction * Physics.ENEMY_PATROL_SPEED;
        }
    }

    private void preventFalling(Level level, double dt) {
        if (!onGround) {
            return;
        }
        double probeX = direction > 0 ? right() + 6 : left() - 6;
        if (!solidBelow(level, probeX, bottom() + 6)) {
            direction = -direction;
            stuckTimer = 0;
        }
        if (Math.abs(velX) < 0.4) {
            stuckTimer += dt;
            if (stuckTimer > 0.3) {
                direction = -direction;
                stuckTimer = 0;
            }
        } else {
            stuckTimer = 0;
        }
    }

    // ---- Tua ngược bản thân ------------------------------------------------
    private void recordHistory() {
        tickCounter++;
        if (tickCounter % SAMPLE_EVERY != 0) {
            return;
        }
        history.addLast(new double[]{x, y, health});
        int capacity = (int) (REWIND_SECONDS * 60 / SAMPLE_EVERY);
        while (history.size() > capacity) {
            history.pollFirst();
        }
    }

    @Override
    protected void onHit(double sourceX) {
        if (state == State.REWINDING || rewindCooldown > 0 || history.size() < 8 || health <= 2) {
            return;
        }
        state = State.REWINDING;
        rewindTimer = 0.55;
        rewindCooldown = REWIND_COOLDOWN;
        rewindHealPending = 2;
        setInvulnerable(0.6);
    }

    private void stepRewind(Level level, double dt) {
        rewindTimer -= dt;
        velX = 0;
        velY = 0;

        if (rewindTimer > 0) {
            double[] snap = history.pollLast();
            if (snap != null) {
                level.spawn(new Particle(centerX(), centerY(),
                        (Math.random() - 0.5) * 70, (Math.random() - 0.5) * 70,
                        new Color(0xC08CF0), 0.35, 5, false));
                x = snap[0];
                y = snap[1];
                health = Collision.clampInt((int) snap[2] + (int) Math.ceil(rewindHealPending), 1, maxHealth);
            }
        } else {
            state = State.CHASE;
            rewindHealPending = 0;
            level.camera().shake(6, 0.25);
            Particle.burst(level, centerX(), centerY(), 16, 150, new Color(0xC08CF0), 0.5);
        }
    }

    // ---- Vẽ ----------------------------------------------------------------
    @Override
    public void render(Graphics2D g) {
        int cx = (int) Math.round(centerX());
        int by = (int) Math.round(bottom());
        int w = (int) Math.round(width);
        int h = (int) Math.round(height);
        int rx = cx - w / 2;
        int ry = by - h;

        // Vệt tua ngược
        if (state == State.REWINDING) {
            g.setColor(new Color(192, 140, 240, 70));
            for (int i = 1; i <= 3; i++) {
                g.fillRoundRect(rx - i * 10, ry, w, h, 8, 8);
            }
        }

        // Áo choàng
        g.setColor(CAPE);
        int capeDir = facingRight ? -1 : 1;
        g.fillPolygon(new Polygon(
                new int[]{cx, cx + capeDir * 20, cx + capeDir * 10},
                new int[]{ry + 12, ry + h - 6, ry + h - 2}, 3));

        // Chân
        g.setColor(new Color(0x1B2233));
        g.fillRect(rx + 4, by - 12, 10, 12);
        g.fillRect(rx + w - 14, by - 12, 10, 12);

        // Thân giáp
        g.setColor(PLATE);
        g.fillRoundRect(rx, ry + 12, w, h - 24, 8, 8);
        g.setColor(PLATE_LIGHT);
        g.fillRoundRect(rx + 3, ry + 15, w - 6, 7, 5, 5);

        // Đồng hồ trên ngực
        int coreAlpha = (int) (140 + 90 * Math.sin(age * 4));
        g.setColor(new Color(VISOR.getRed(), VISOR.getGreen(), VISOR.getBlue(), coreAlpha));
        g.drawOval(cx - 7, ry + 24, 14, 14);
        g.drawLine(cx, ry + 31, cx, ry + 24);
        g.drawLine(cx, ry + 31, cx + 5, ry + 34);

        // Mũ giáp
        g.setColor(PLATE_LIGHT);
        g.fillRoundRect(rx + 2, ry - 2, w - 4, 18, 8, 8);
        g.setColor(new Color(0x0E1622));
        g.fillRect(rx + 4, ry + 5, w - 8, 7);
        g.setColor(VISOR);
        g.fillRect(cx + (facingRight ? 0 : -8), ry + 6, 8, 5);

        // Kiếm
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0xC9D6E8));
        int swordDir = facingRight ? 1 : -1;
        g.drawLine(cx + swordDir * 10, ry + 26, cx + swordDir * 34, ry + 6);
        g.setStroke(old);

        if (state == State.LUNGE) {
            g.setColor(new Color(255, 120, 120, 120));
            g.fillRoundRect(rx - 4, ry - 4, w + 8, h + 8, 10, 10);
        }
    }
}
