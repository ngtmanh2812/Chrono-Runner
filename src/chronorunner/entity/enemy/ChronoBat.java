package chronorunner.entity.enemy;

import chronorunner.core.Physics;
import chronorunner.core.TimeState;
import chronorunner.entity.Player;
import chronorunner.util.Collision;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * Chrono Bat - quái vật bay.
 *
 * <p>Bay lơ lửng quanh vị trí tổ, khi người chơi tới gần thì lao tới đuổi theo.
 * Vì bay nên nó bỏ qua địa hình - người chơi phải né hoặc đánh trúng trên không.</p>
 */
public class ChronoBat extends Enemy {

    private static final Color WING = new Color(0x3B2456);
    private static final Color BODY = new Color(0x8A5BC4);
    private static final double DETECT_RANGE = 360;

    private final double homeX;
    private final double homeY;

    private double wingPhase;
    private boolean chasing;

    public ChronoBat(double x, double y) {
        super(x, y, 30, 20, 1);
        this.homeX = x;
        this.homeY = y;
        this.contactDamage = 1;
        this.coinReward = 5;
        this.gravityScale = 0;
        this.freezable = true;
    }

    @Override
    protected void think(Level level, double dt) {
        wingPhase += dt * 14;

        Player p = level.player();
        double targetX = homeX;
        double targetY = homeY + Math.sin(age * 1.6) * 26;

        if (p != null && p.isAlive() && distanceTo(p) < DETECT_RANGE) {
            chasing = true;
            targetX = p.centerX();
            targetY = p.centerY() - 6;
        } else {
            chasing = false;
        }

        double speed = chasing ? Physics.BAT_CHASE_SPEED : 62;
        double dx = targetX - centerX();
        double dy = targetY - centerY();
        double dist = Math.max(1, Math.hypot(dx, dy));

        velX = Collision.approach(velX, dx / dist * speed, 460 * dt);
        velY = Collision.approach(velY, dy / dist * speed, 460 * dt);

        x += velX * dt;
        y += velY * dt;

        // Bay trong lòng màn chơi, không xuyên ra ngoài
        x = Collision.clamp(x, 0, level.worldWidth() - width);
        y = Collision.clamp(y, 0, level.worldHeight() - height);

        faceTowards(centerX() + Collision.sign(velX));
        checkOutOfBounds(level);
    }

    /** Bat bay nên có thể bị tiêu diệt bằng một cú nhảy trúng đầu. */
    @Override
    protected void onDeath() {
        super.onDeath();
    }

    @Override
    public void render(Graphics2D g) {
        int cx = (int) Math.round(centerX());
        int cy = (int) Math.round(centerY());

        // Cánh vỗ
        double flap = Math.sin(wingPhase) * 8;
        g.setColor(WING);
        g.fillPolygon(new Polygon(
                new int[]{cx - 3, cx - 22, cx - 16},
                new int[]{cy - 3, cy - 8 + (int) flap, cy + 8}, 3));
        g.fillPolygon(new Polygon(
                new int[]{cx + 3, cx + 22, cx + 16},
                new int[]{cy - 3, cy - 8 + (int) flap, cy + 8}, 3));

        // Thân
        g.setColor(BODY);
        g.fillOval(cx - 10, cy - 9, 20, 18);
        g.setColor(new Color(0x2A1740));
        g.fillOval(cx - 7, cy - 6, 14, 12);

        // Tai
        g.setColor(WING);
        g.fillPolygon(new int[]{cx - 7, cx - 3, cx - 8}, new int[]{cy - 7, cy - 15, cy - 8}, 3);
        g.fillPolygon(new int[]{cx + 7, cx + 3, cx + 8}, new int[]{cy - 7, cy - 15, cy - 8}, 3);

        // Mắt
        Color eye = chasing ? new Color(0xFF4D4D) : new Color(0xFFB86B);
        g.setColor(eye);
        g.fillOval(cx - 6, cy - 3, 4, 4);
        g.fillOval(cx + 2, cy - 3, 4, 4);

        if (chasing) {
            g.setColor(new Color(255, 77, 77, 90));
            g.drawOval(cx - 18, cy - 16, 36, 32);
        }
    }

    /** Bat chỉ sống ở HIỆN TẠI và TƯƠNG LAI. */
    public static ChronoBat futureVariant(double x, double y) {
        ChronoBat bat = new ChronoBat(x, y);
        bat.setTimeMask(TimeState.MASK_PRESENT | TimeState.MASK_FUTURE);
        return bat;
    }
}
