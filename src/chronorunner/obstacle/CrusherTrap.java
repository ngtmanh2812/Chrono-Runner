package chronorunner.obstacle;

import chronorunner.core.Physics;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Bẫy nghiền: khối đá nâng lên rồi đập xuống theo chu kỳ.
 *
 * <p>Chu kỳ gồm 4 pha: nghỉ -> cảnh báo (nhấp nháy đỏ) -> đập xuống -> thu về.
 * Sát thương chỉ có tác dụng ở pha đập xuống.</p>
 */
public class CrusherTrap extends Trap {

    private enum Phase {
        IDLE, WARNING, SLAM, RETRACT
    }

    private static final double IDLE_TIME = 1.5;
    private static final double WARNING_TIME = 0.5;
    private static final double SLAM_TIME = 0.9;

    private final double restY;
    private final double travel;
    private final double speed;

    private Phase phase = Phase.IDLE;
    private double timer = IDLE_TIME;

    /**
     * @param travel quãng đường khối đá rơi xuống (pixel)
     */
    public CrusherTrap(double x, double y, double w, double h, double travel, double speed) {
        super(x, y, w, h);
        this.restY = y;
        this.travel = travel;
        this.speed = speed;
        this.damage = 1;
        this.solid = false;
    }

    @Override
    protected void tick(Level level, double dt) {
        switch (phase) {
            case IDLE:
                timer -= dt;
                if (timer <= 0) {
                    phase = Phase.WARNING;
                    timer = WARNING_TIME;
                }
                armed = false;
                break;
            case WARNING:
                timer -= dt;
                armed = false;
                if (timer <= 0) {
                    phase = Phase.SLAM;
                    velY = 120;
                }
                break;
            case SLAM:
                velY += Physics.GRAVITY * 1.4 * dt;
                velY = Math.min(velY, speed);
                y += velY * dt;
                armed = true;
                if (y >= restY + travel) {
                    y = restY + travel;
                    velY = 0;
                    level.camera().shake(7, 0.28);
                    phase = Phase.RETRACT;
                    timer = SLAM_TIME * 0.45;
                    armed = true;
                }
                break;
            case RETRACT:
                timer -= dt;
                armed = false;
                y -= speed * 0.6 * dt;
                if (y <= restY) {
                    y = restY;
                    phase = Phase.IDLE;
                    timer = IDLE_TIME;
                }
                break;
            default:
        }
    }

    @Override
    public void render(Graphics2D g) {
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rw = (int) Math.round(width);
        int rh = (int) Math.round(height);

        // Xích / trục treo phía trên
        g.setColor(new Color(0x2E2E38));
        g.fillRect((int) Math.round(centerX()) - 3, (int) restY - 400, 6, 400);

        Color body = phase == Phase.WARNING
                ? new Color(0xC0392B)
                : new Color(0x3A3F4B);
        g.setColor(body);
        g.fillRect(rx, ry, rw, rh);

        g.setColor(new Color(0, 0, 0, 120));
        g.drawRect(rx, ry, rw - 1, rh - 1);

        // Răng nghiền phía dưới
        g.setColor(new Color(0x8A8F9B));
        int teeth = Math.max(2, rw / 10);
        int tw = rw / teeth;
        for (int i = 0; i < teeth; i++) {
            int tx = rx + i * tw;
            g.fillPolygon(new int[]{tx, tx + tw, tx + tw / 2},
                    new int[]{ry + rh, ry + rh, ry + rh + 7}, 3);
        }

        if (phase == Phase.WARNING) {
            g.setColor(new Color(255, 80, 60, (int) (120 + 100 * Math.sin(age * 40))));
            g.fillRect(rx - 2, ry - 2, rw + 4, rh + 4);
        }
    }
}
