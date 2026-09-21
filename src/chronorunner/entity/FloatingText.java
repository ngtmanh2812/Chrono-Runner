package chronorunner.entity;

import chronorunner.util.Collision;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Dòng chữ bay lên rồi mờ dần: "+5", "NĂNG LƯỢNG +", sát thương... */
public class FloatingText extends GameObject {

    private final String text;
    private final Color color;
    private final double maxLife;
    private double life;

    public FloatingText(double x, double y, String text, Color color, double life) {
        super(x, y, 1, 1);
        this.text = text;
        this.color = color;
        this.life = life;
        this.maxLife = life;
        this.velY = -46;
        this.visible = false;
    }

    @Override
    public void update(Level level, double dt) {
        age += dt;
        life -= dt;
        if (life <= 0) {
            alive = false;
            return;
        }
        velY += 60 * dt;
        x += velX * dt;
        y += velY * dt;
    }

    @Override
    public void render(Graphics2D g) {
        double t = Collision.clamp(life / maxLife, 0, 1);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 15f));
        int alpha = (int) (255 * Math.min(1, t * 2));
        g.setColor(new Color(0, 0, 0, alpha / 2));
        g.drawString(text, (int) Math.round(x) + 1, (int) Math.round(y) + 1);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.drawString(text, (int) Math.round(x), (int) Math.round(y));
    }
}
