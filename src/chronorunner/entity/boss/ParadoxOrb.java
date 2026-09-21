package chronorunner.entity.boss;

import chronorunner.entity.Projectile;

import java.awt.Color;
import java.awt.Graphics2D;

/** Quả cầu nghịch lý - đạn của The Paradox. */
public class ParadoxOrb extends Projectile {

    private static final Color CORE = new Color(0xFF5FD2);
    private static final Color RING = new Color(0xC08CF0);

    public ParadoxOrb(double x, double y, double dirX, double dirY, double speed, int damage) {
        super(x, y, 14, 14, dirX, dirY, speed);
        this.damage = damage;
        this.color = CORE;
        this.life = 4.5;
    }

    @Override
    protected void drawShape(Graphics2D g, int cx, int cy, int r) {
        g.setColor(new Color(0x2A0B33));
        g.fillOval(cx - r, cy - r, r * 2, r * 2);

        double a = age * 6;
        g.setColor(RING);
        g.drawOval(cx - r - 3, cy - r, r * 2 + 6, r * 2);
        g.drawOval(cx - r, cy - r - 3, r * 2, r * 2 + 6);

        g.setColor(CORE);
        g.fillOval(cx - r / 2, cy - r / 2, r, r);

        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval(cx - 2, cy - 2, 4, 4);

        g.setColor(new Color(CORE.getRed(), CORE.getGreen(), CORE.getBlue(), 90));
        g.drawLine(cx + (int) (Math.cos(a) * (r + 6)), cy + (int) (Math.sin(a) * (r + 6)),
                cx - (int) (Math.cos(a) * (r + 6)), cy - (int) (Math.sin(a) * (r + 6)));
    }
}
