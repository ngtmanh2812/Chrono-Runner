package chronorunner.entity;

import chronorunner.core.Physics;
import chronorunner.util.Collision;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Hạt hiệu ứng: dùng cho nổ, bụi khi tiếp đất, tia sáng khi đổi thời đại...
 *
 * <p>Hạt không tham gia va chạm, chỉ mờ dần rồi tự huỷ.</p>
 */
public class Particle extends GameObject {

    private final Color color;
    private final double maxLife;
    private double life;
    private final double size;
    private final boolean useGravity;

    public Particle(double x, double y, double vx, double vy, Color color, double life) {
        this(x, y, vx, vy, color, life, 4, true);
    }

    public Particle(double x, double y, double vx, double vy, Color color,
                    double life, double size, boolean useGravity) {
        super(x, y, size, size);
        this.velX = vx;
        this.velY = vy;
        this.color = color;
        this.life = life;
        this.maxLife = life;
        this.size = size;
        this.useGravity = useGravity;
    }

    /** Chùm hạt tròn toả ra từ một điểm. */
    public static void burst(Level level, double cx, double cy, int count,
                             double speed, Color color, double life) {
        for (int i = 0; i < count; i++) {
            double ang = Math.PI * 2 * i / count + Math.random() * 0.4;
            double sp = speed * (0.5 + Math.random() * 0.6);
            level.spawn(new Particle(cx, cy,
                    Math.cos(ang) * sp, Math.sin(ang) * sp, color, life));
        }
    }

    @Override
    public void update(Level level, double dt) {
        age += dt;
        life -= dt;
        if (life <= 0) {
            alive = false;
            return;
        }
        if (useGravity) {
            velY += Physics.GRAVITY * 0.35 * dt;
        }
        x += velX * dt;
        y += velY * dt;
    }

    @Override
    public void render(Graphics2D g) {
        double t = Collision.clamp(life / maxLife, 0, 1);
        int alpha = (int) (255 * t);
        int s = Math.max(1, (int) Math.round(size * (0.4 + 0.6 * t)));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.fillOval((int) Math.round(x), (int) Math.round(y), s, s);
    }
}
