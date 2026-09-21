package chronorunner.obstacle;

import chronorunner.entity.Player;
import chronorunner.world.Level;

/**
 * Lớp cơ sở cho bẫy.
 *
 * <p>Bẫy có hai pha: "lên đạn" (cảnh báo, chưa gây sát thương) và
 * "kích hoạt" (gây sát thương khi chạm). Nhờ vậy người chơi luôn có
 * cơ hội phản ứng thay vì bị đánh úp.</p>
 */
public abstract class Trap extends Obstacle {

    protected int damage = 1;
    protected boolean armed;

    protected Trap(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    public int damage() {
        return damage;
    }

    public boolean isArmed() {
        return armed;
    }

    @Override
    public void update(Level level, double dt) {
        super.update(level, dt);
        tick(level, dt);
        if (armed) {
            hitPlayer(level);
        }
    }

    /** Nhịp hoạt động riêng của từng loại bẫy. */
    protected abstract void tick(Level level, double dt);

    protected void hitPlayer(Level level) {
        Player p = level.player();
        if (p != null && p.isAlive() && intersects(p)) {
            p.damage(damage, centerX());
        }
    }
}
