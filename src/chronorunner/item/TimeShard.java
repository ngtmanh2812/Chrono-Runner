package chronorunner.item;

import chronorunner.entity.FloatingText;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * Mảnh vỡ Chrono Core - vật phẩm quan trọng nhất.
 *
 * <p>Time Shard dùng để mở cổng ra của màn chơi và để nâng cấp Chrono Core.</p>
 */
public class TimeShard extends Item {

    private static final Color CYAN = new Color(0x7FE9FF);

    public TimeShard(double x, double y) {
        super(x, y, 18);
    }

    @Override
    public void apply(Player player, Level level) {
        level.addShard();
        level.game().toast("Time Shard " + level.shardsCollected() + "/" + level.totalShards());
        player.addTimeEnergy(8);
    }

    @Override
    protected void onCollected(Level level, Player p) {
        super.onCollected(level, p);
        level.spawn(new FloatingText(centerX() - 8, top() - 6, "+1 SHARD", CYAN, 0.9));
        level.camera().shake(3, 0.16);
    }

    @Override
    public Color color() {
        return CYAN;
    }

    @Override
    protected void renderShape(Graphics2D g, int cx, int cy, int size) {
        int r = size / 2;
        Polygon diamond = new Polygon(
                new int[]{cx, cx + r, cx, cx - r},
                new int[]{cy - r - 3, cy, cy + r + 3, cy},
                4);
        g.setColor(new Color(0x0B3A4A));
        g.fillPolygon(diamond);
        g.setColor(CYAN);
        g.drawPolygon(diamond);

        // Lõi sáng bên trong
        g.setColor(new Color(255, 255, 255, 200));
        g.fillPolygon(new Polygon(
                new int[]{cx, cx + r / 2, cx, cx - r / 2},
                new int[]{cy - r / 2, cy, cy + r / 2, cy}, 4));

        // Tia sáng quay
        g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 150));
        double a = spin;
        g.drawLine(cx + (int) (Math.cos(a) * (r + 4)), cy + (int) (Math.sin(a) * (r + 4)),
                cx - (int) (Math.cos(a) * (r + 4)), cy - (int) (Math.sin(a) * (r + 4)));
    }
}
