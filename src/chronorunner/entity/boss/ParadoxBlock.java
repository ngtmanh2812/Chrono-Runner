package chronorunner.entity.boss;

import chronorunner.entity.Particle;
import chronorunner.obstacle.Platform;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Khối nền do The Paradox tạo ra bằng cách bẻ cong dòng thời gian.
 *
 * <p>Tồn tại trong thời gian ngắn rồi tự tan biến, khiến mặt sàn dưới chân
 * người chơi biến mất đúng lúc nguy hiểm nhất.</p>
 */
public class ParadoxBlock extends Platform {

    private static final Color FILL = new Color(0x6B2E8E);
    private static final Color EDGE = new Color(0xFF5FD2);

    private double life;
    private final double maxLife;

    public ParadoxBlock(double x, double y, double w, double h, double life) {
        super(x, y, w, h);
        this.life = life;
        this.maxLife = life;
        this.oneWay = false;
        this.bodyColor = FILL;
        this.edgeColor = EDGE;
    }

    @Override
    protected void move(Level level, double dt) {
        // đứng yên tại chỗ
    }

    @Override
    public void update(Level level, double dt) {
        super.update(level, dt);
        life -= dt;
        if (life <= 0) {
            alive = false;
            Particle.burst(level, centerX(), centerY(), 12, 130, EDGE, 0.45);
        }
    }

    /** Xoá sớm khối nền (trùm chủ động "xoá platform"). */
    public void dissolve(Level level) {
        if (!alive) {
            return;
        }
        alive = false;
        Particle.burst(level, centerX(), centerY(), 12, 130, EDGE, 0.45);
    }

    @Override
    public void render(Graphics2D g) {
        double t = Math.max(0, Math.min(1, life / maxLife));
        int alpha = (int) (90 + 140 * t);
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rw = (int) Math.round(width);
        int rh = (int) Math.round(height);

        g.setColor(new Color(FILL.getRed(), FILL.getGreen(), FILL.getBlue(), alpha));
        g.fillRect(rx, ry, rw, rh);
        g.setColor(new Color(EDGE.getRed(), EDGE.getGreen(), EDGE.getBlue(), alpha));
        g.drawRect(rx, ry, rw, rh);
        g.drawLine(rx, ry + rh / 2, rx + rw, ry + rh / 2);
    }
}
