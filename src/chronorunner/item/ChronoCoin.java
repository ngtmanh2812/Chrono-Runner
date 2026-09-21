package chronorunner.item;

import chronorunner.core.Physics;
import chronorunner.entity.FloatingText;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;

/** Đồng xu Chrono - tiền tệ dùng để mua nâng cấp. */
public class ChronoCoin extends Item {

    private static final Color GOLD = new Color(0xFFD166);

    private final int value;

    public ChronoCoin(double x, double y) {
        this(x, y, Physics.COIN_VALUE);
    }

    public ChronoCoin(double x, double y, int value) {
        super(x, y, 14);
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public void apply(Player player, Level level) {
        level.game().addCoins(value);
        level.addCoinCollected();
    }

    @Override
    protected void onCollected(Level level, Player p) {
        super.onCollected(level, p);
        level.spawn(new FloatingText(centerX() - 8, top() - 4, "+" + value, GOLD, 0.7));
    }

    @Override
    public Color color() {
        return GOLD;
    }

    @Override
    protected void renderShape(Graphics2D g, int cx, int cy, int size) {
        int r = size / 2;
        // Đồng xu "dẹt" theo chu kỳ để tạo cảm giác xoay
        int w = Math.max(3, (int) (r * Math.abs(Math.cos(spin))));
        g.setColor(new Color(0x6B4E12));
        g.fillOval(cx - w, cy - r, w * 2, r * 2);
        g.setColor(GOLD);
        g.fillOval(cx - w + 1, cy - r + 1, w * 2 - 2, r * 2 - 2);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 10f));
        g.setColor(new Color(0x5C4310));
        if (w > 5) {
            g.drawString("C", cx - 4, cy + 4);
        }
    }
}
