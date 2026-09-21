package chronorunner.entity.enemy;

import chronorunner.entity.Projectile;

import java.awt.Color;
import java.awt.Graphics2D;

/** Tia năng lượng do Time Guardian bắn ra. */
public class GuardianBolt extends Projectile {

    private static final Color BOLT = new Color(0x8BE0FF);

    public GuardianBolt(double x, double y, double dirX, double dirY) {
        super(x, y, 12, 12, dirX, dirY, 260);
        this.damage = 1;
        this.color = BOLT;
        this.life = 3.4;
    }

    @Override
    protected void drawShape(Graphics2D g, int cx, int cy, int r) {
        g.setColor(new Color(0x0E3A4A));
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setColor(BOLT);
        g.drawOval(cx - r, cy - r, r * 2, r * 2);

        g.setColor(new Color(255, 255, 255, 200));
        g.fillOval(cx - r / 2, cy - r / 2, r, r);

        // Đuôi năng lượng
        g.setColor(new Color(BOLT.getRed(), BOLT.getGreen(), BOLT.getBlue(), 110));
        g.drawLine(cx, cy, cx - (int) Math.signum(velX) * (r + 8), cy - (int) Math.signum(velY) * (r + 8));
    }
}
