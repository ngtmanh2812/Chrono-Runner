package chronorunner.item;

import chronorunner.core.TimeState;
import chronorunner.entity.GameObject;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Lớp cơ sở cho vật phẩm nhặt được.
 *
 * <p>Vật phẩm tự kiểm tra va chạm với người chơi mỗi bước cập nhật;
 * khi được nhặt, lớp con quyết định hiệu ứng qua {@link #apply(Player, Level)}.</p>
 */
public abstract class Item extends GameObject {

    /** Bán kính hút: vật phẩm bay về phía người chơi khi ở gần. */
    private static final double MAGNET_RANGE = 78;
    private static final double MAGNET_SPEED = 420;

    protected double bobPhase;
    protected double spin;

    protected Item(double x, double y, double size) {
        super(x, y, size, size);
        this.timeMask = TimeState.ALL;
        this.solid = false;
        this.bobPhase = Math.random() * Math.PI * 2;
    }

    /** Hiệu ứng khi người chơi nhặt được vật phẩm. */
    public abstract void apply(Player player, Level level);

    /** Màu chủ đạo, dùng cho vòng sáng và hiệu ứng hạt. */
    public abstract Color color();

    @Override
    public void update(Level level, double dt) {
        age += dt;
        spin += dt * 2.4;

        Player p = level.player();
        if (p == null || !p.isAlive()) {
            return;
        }

        double d = Math.hypot(p.centerX() - centerX(), p.centerY() - centerY());
        if (d < MAGNET_RANGE) {
            // Hút về phía người chơi
            double nx = (p.centerX() - centerX()) / Math.max(1, d);
            double ny = (p.centerY() - centerY()) / Math.max(1, d);
            double pull = MAGNET_SPEED * (1 - d / MAGNET_RANGE) * dt;
            x += nx * pull;
            y += ny * pull;
        }

        if (intersects(p.getX() - 4, p.getY() - 4, p.getWidth() + 8, p.getHeight() + 8)) {
            apply(p, level);
            onCollected(level, p);
            alive = false;
        }
    }

    protected void onCollected(Level level, Player p) {
        for (int i = 0; i < 10; i++) {
            double ang = Math.random() * Math.PI * 2;
            level.spawn(new chronorunner.entity.Particle(centerX(), centerY(),
                    Math.cos(ang) * 90, Math.sin(ang) * 90, color(), 0.4, 3, false));
        }
    }

    @Override
    public void render(Graphics2D g) {
        double bob = Math.sin(age * 3 + bobPhase) * 3.5;
        int cx = (int) Math.round(centerX());
        int cy = (int) Math.round(centerY() + bob);
        int size = (int) Math.round(width);

        int glow = (int) (60 + 40 * Math.sin(age * 4 + bobPhase));
        g.setColor(new Color(color().getRed(), color().getGreen(), color().getBlue(), glow));
        g.fillOval(cx - size, cy - size, size * 2, size * 2);

        renderShape(g, cx, cy, size);
    }

    /** Vẽ hình dạng riêng của từng loại vật phẩm. */
    protected abstract void renderShape(Graphics2D g, int cx, int cy, int size);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[x=" + (int) x + " y=" + (int) y + "]";
    }
}
