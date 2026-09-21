package chronorunner.entity.enemy;

import chronorunner.core.Physics;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Time Guardian - robot canh gác các cổng thời gian.
 *
 * <p>Có vùng phát hiện: khi người chơi bước vào, nó đuổi theo và bắn tia
 * năng lượng. Ra khỏi vùng thì quay lại đi tuần.</p>
 */
public class TimeGuardian extends Enemy {

    private static final Color METAL = new Color(0x5A6472);
    private static final Color METAL_DARK = new Color(0x333B47);
    private static final Color EYE_IDLE = new Color(0x7FD4FF);
    private static final Color EYE_ALERT = new Color(0xFF5A4D);

    private static final double SHOOT_COOLDOWN = 1.7;

    private int direction = 1;
    private double stuckTimer;
    private double shootCooldown;
    private double scanPhase;
    private boolean alerted;

    public TimeGuardian(double x, double y) {
        super(x, y, 30, 40, 4);
        this.contactDamage = 1;
        this.coinReward = 12;
        this.stompable = true;
    }

    public boolean isAlerted() {
        return alerted;
    }

    @Override
    protected void think(Level level, double dt) {
        scanPhase += dt * 3;
        applyGravity(level, dt);

        Player p = level.player();
        alerted = p != null && p.isAlive() && distanceTo(p) < Physics.GUARDIAN_DETECT_RANGE;

        double prevX = x;

        if (alerted) {
            faceTowards(p.centerX());
            direction = p.centerX() > centerX() ? 1 : -1;
            boolean sameLevel = Math.abs(p.centerY() - centerY()) < 70;
            if (sameLevel && distanceTo(p) > 60) {
                velX = direction * Physics.GUARDIAN_CHASE_SPEED;
            } else {
                velX = 0;
            }

            shootCooldown -= dt;
            if (shootCooldown <= 0 && distanceTo(p) < 420) {
                shoot(level, p);
                shootCooldown = SHOOT_COOLDOWN;
            }
        } else {
            shootCooldown = Math.min(shootCooldown, 0.6);
            if (onGround) {
                velX = direction * Physics.ENEMY_PATROL_SPEED * 0.8;
                double probeX = direction > 0 ? right() + 6 : left() - 6;
                if (!solidBelow(level, probeX, bottom() + 6)) {
                    direction = -direction;
                }
            }
        }

        moveWithCollision(level, dt);

        if (onGround && Math.abs(x - prevX) < 0.4) {
            stuckTimer += dt;
            if (stuckTimer > 0.25) {
                direction = -direction;
                stuckTimer = 0;
            }
        } else {
            stuckTimer = 0;
        }

        checkOutOfBounds(level);
    }

    private void shoot(Level level, Player target) {
        double dx = target.centerX() - centerX();
        double dy = target.centerY() - centerY();
        level.spawn(new GuardianBolt(centerX() - 6, centerY() - 6, dx, dy));
        level.camera().shake(2, 0.1);
    }

    @Override
    protected void onHit(double sourceX) {
        // Bị đánh trúng thì lập tức cảnh giác
        alerted = true;
        shootCooldown = Math.min(shootCooldown, 0.35);
    }

    @Override
    public void render(Graphics2D g) {
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rw = (int) Math.round(width);
        int rh = (int) Math.round(height);

        Color eye = alerted ? EYE_ALERT : EYE_IDLE;

        // Chân
        g.setColor(METAL_DARK);
        g.fillRect(rx + 3, ry + rh - 10, 8, 10);
        g.fillRect(rx + rw - 11, ry + rh - 10, 8, 10);

        // Thân
        g.setColor(METAL);
        g.fillRoundRect(rx, ry + 8, rw, rh - 16, 6, 6);
        g.setColor(METAL_DARK);
        g.drawRoundRect(rx, ry + 8, rw, rh - 16, 6, 6);

        // Giáp vai
        g.setColor(new Color(0x7C8798));
        g.fillRect(rx - 3, ry + 10, 5, 12);
        g.fillRect(rx + rw - 2, ry + 10, 5, 12);

        // Đầu + mắt
        g.setColor(METAL_DARK);
        g.fillRoundRect(rx + 4, ry - 2, rw - 8, 14, 5, 5);
        int eyeX = rx + rw / 2 + (int) (Math.sin(scanPhase) * 5);
        g.setColor(eye);
        g.fillOval(eyeX - 4, ry + 2, 8, 6);
        g.setColor(new Color(eye.getRed(), eye.getGreen(), eye.getBlue(), 70));
        g.fillOval(eyeX - 9, ry - 3, 18, 16);

        // Vòng năng lượng quanh thân
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(eye.getRed(), eye.getGreen(), eye.getBlue(), 120));
        double a = age * 2.2;
        g.drawLine(rx - 4 + (int) (Math.cos(a) * 16), ry + rh / 2 + (int) (Math.sin(a) * 14),
                rx + rw + 4 - (int) (Math.cos(a) * 16), ry + rh / 2 - (int) (Math.sin(a) * 14));
        g.setStroke(old);

        if (alerted) {
            g.setColor(new Color(255, 90, 77, 200));
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 13f));
            g.drawString("!", rx + rw / 2 - 2, ry - 6);
        }
    }
}
