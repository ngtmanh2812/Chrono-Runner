package chronorunner.item;

import chronorunner.entity.FloatingText;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/** Tinh thể thời gian: hồi năng lượng cho Chrono Core. */
public class TimeCrystal extends Item {

    private static final Color VIOLET = new Color(0xC08CF0);
    /** Lượng năng lượng hồi lại khi nhặt. */
    public static final double ENERGY_RESTORE = 40;

    public TimeCrystal(double x, double y) {
        super(x, y, 16);
    }

    @Override
    public void apply(Player player, Level level) {
        player.addTimeEnergy(ENERGY_RESTORE);
    }

    @Override
    protected void onCollected(Level level, Player p) {
        super.onCollected(level, p);
        level.spawn(new FloatingText(centerX() - 16, top() - 6, "+NĂNG LƯỢNG", VIOLET, 0.9));
    }

    @Override
    public Color color() {
        return VIOLET;
    }

    @Override
    protected void renderShape(Graphics2D g, int cx, int cy, int size) {
        int r = size / 2;
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 3 * i - Math.PI / 6;
            hex.addPoint(cx + (int) (Math.cos(a) * r), cy + (int) (Math.sin(a) * (r + 2)));
        }
        g.setColor(new Color(0x3A1B5C));
        g.fillPolygon(hex);
        g.setColor(VIOLET);
        g.drawPolygon(hex);

        g.setColor(new Color(255, 255, 255, 170));
        g.fillPolygon(new Polygon(
                new int[]{cx, cx + r / 3, cx},
                new int[]{cy - r, cy, cy + r}, 3));
    }
}
