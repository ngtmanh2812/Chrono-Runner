package chronorunner.entity.enemy;

import chronorunner.core.Physics;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Time Slime - kẻ địch cơ bản.
 *
 * <p>Đi tuần qua lại trên mặt đất, tự quay đầu khi gặp tường hoặc khi
 * phía trước không còn chỗ đứng (nhờ vậy không rơi xuống vực).</p>
 */
public class TimeSlime extends Enemy {

    private static final Color BODY = new Color(0x4FBF6A);
    private static final Color BODY_DARK = new Color(0x2E7A44);

    private int direction = 1;
    private double stuckTimer;
    private double wobble;

    public TimeSlime(double x, double y) {
        super(x, y, 28, 24, 2);
        this.contactDamage = 1;
        this.coinReward = 5;
        this.direction = Math.random() < 0.5 ? -1 : 1;
    }

    @Override
    protected void think(Level level, double dt) {
        wobble += dt * 5;
        applyGravity(level, dt);

        double prevX = x;
        if (onGround) {
            velX = direction * Physics.ENEMY_PATROL_SPEED;

            // Sắp rơi xuống vực -> quay đầu
            double probeX = direction > 0 ? right() + 6 : left() - 6;
            if (!solidBelow(level, probeX, bottom() + 6)) {
                direction = -direction;
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

        faceTowards(centerX() + direction * 10);
        checkOutOfBounds(level);
    }

    @Override
    public void render(Graphics2D g) {
        int cx = (int) Math.round(centerX());
        int baseY = (int) Math.round(bottom());
        double stretch = Math.sin(wobble) * 2.5;
        int w = (int) Math.round(width + stretch);
        int h = (int) Math.round(height - stretch);

        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(cx - w / 2, baseY - 5, w, 6);

        g.setColor(BODY_DARK);
        g.fillRoundRect(cx - w / 2, baseY - h, w, h, 14, 14);
        g.setColor(BODY);
        g.fillRoundRect(cx - w / 2 + 2, baseY - h + 2, w - 4, h - 5, 12, 12);

        // Đồng hồ nhỏ bên trong
        g.setColor(new Color(255, 255, 255, 90));
        g.drawOval(cx - 6, baseY - h + 8, 12, 12);
        g.drawLine(cx, baseY - h + 14, cx + (int) (Math.cos(age * 2) * 5), baseY - h + 14 + (int) (Math.sin(age * 2) * 5));

        // Mắt
        int eyeDir = facingRight ? 1 : -1;
        g.setColor(Color.WHITE);
        g.fillOval(cx - 8 + eyeDir * 3, baseY - h + 5, 6, 6);
        g.fillOval(cx + 2 + eyeDir * 3, baseY - h + 5, 6, 6);
        g.setColor(new Color(0x102A18));
        g.fillOval(cx - 6 + eyeDir * 4, baseY - h + 7, 3, 3);
        g.fillOval(cx + 4 + eyeDir * 4, baseY - h + 7, 3, 3);
    }
}
