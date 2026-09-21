package chronorunner.entity;

import chronorunner.entity.enemy.Enemy;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Đạn / năng lượng bay.
 *
 * <p>Đạn của kẻ địch có thể bị người chơi đánh bật lại
 * ({@link #deflect(double)}) và khi đó nó quay sang gây sát thương cho chính
 * kẻ địch - một cơ chế thưởng cho người chơi phản xạ tốt.</p>
 */
public abstract class Projectile extends GameObject {

    protected int damage = 1;
    protected double life = 4.0;
    protected boolean hostile = true;
    protected double speed = 240;
    protected Color color = new Color(0x9FE8FF);

    protected Projectile(double x, double y, double w, double h, double dirX, double dirY, double speed) {
        super(x, y, w, h);
        double len = Math.hypot(dirX, dirY);
        if (len == 0) {
            len = 1;
        }
        this.speed = speed;
        this.velX = dirX / len * speed;
        this.velY = dirY / len * speed;
    }

    public boolean isHostile() {
        return hostile;
    }

    public int damage() {
        return damage;
    }

    public Color color() {
        return color;
    }

    /** Bị đánh bật: đổi phe và quay đầu. */
    public void deflect(double fromX) {
        hostile = false;
        if (centerX() < fromX) {
            velX = -Math.abs(velX);
        } else {
            velX = Math.abs(velX);
        }
        velY *= -0.35;
        damage += 1;
    }

    @Override
    public void update(Level level, double dt) {
        age += dt;
        life -= dt;
        if (life <= 0) {
            alive = false;
            return;
        }
        if (level.isTimeFrozen() && hostile) {
            return;
        }

        x += velX * dt;
        y += velY * dt;

        if (level.solidInRect(x, y, width, height)) {
            explode(level);
            return;
        }
        if (x < -80 || x > level.worldWidth() + 80 || y < -120 || y > level.worldHeight() + 120) {
            alive = false;
            return;
        }

        if (hostile) {
            Player p = level.player();
            if (p != null && p.isAlive() && intersects(p.getX(), p.getY(), p.getWidth(), p.getHeight())) {
                p.damage(damage, centerX());
                explode(level);
            }
        } else {
            for (Enemy e : level.enemies()) {
                if (e.isAlive() && e.isDamageable() && intersects(e)) {
                    e.takeDamage(damage, centerX());
                    explode(level);
                    return;
                }
            }
        }
    }

    protected void explode(Level level) {
        alive = false;
        Particle.burst(level, centerX(), centerY(), 10, 130, color, 0.35);
    }

    @Override
    public void render(Graphics2D g) {
        int cx = (int) Math.round(centerX());
        int cy = (int) Math.round(centerY());
        int r = (int) Math.round(Math.max(width, height) / 2);

        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70));
        g.fillOval(cx - r * 2, cy - r * 2, r * 4, r * 4);

        g.setColor(color);
        drawShape(g, cx, cy, r);

        if (!hostile) {
            // Đạn đã bị phản: viền trắng để người chơi nhận ra
            g.setColor(Color.WHITE);
            g.drawOval(cx - r - 2, cy - r - 2, r * 2 + 4, r * 2 + 4);
        }
    }

    protected abstract void drawShape(Graphics2D g, int cx, int cy, int r);
}
