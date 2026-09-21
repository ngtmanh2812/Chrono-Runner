package chronorunner.obstacle;

import chronorunner.core.TimeState;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Cổng kết thúc màn chơi.
 *
 * <p>Cổng chỉ mở khi người chơi thu thập đủ số Time Shard tối thiểu,
 * nhờ đó không thể chạy thẳng qua màn mà bỏ hết puzzle.</p>
 */
public class ExitPortal extends Obstacle {

    private static final double SIZE_W = 56;
    private static final double SIZE_H = 88;

    private boolean open;
    private double openPulse;
    /** Số shard cần thiết của màn, cập nhật mỗi bước để hiển thị lên cổng. */
    private int requiredShards = 1;

    public ExitPortal(double x, double y) {
        super(x - SIZE_W / 2, y - SIZE_H, SIZE_W, SIZE_H);
        this.timeMask = TimeState.ALL;
        this.solid = false;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void update(Level level, double dt) {
        super.update(level, dt);

        int required = level.requiredShards();
        requiredShards = required;
        boolean bossAlive = level.boss() != null;
        boolean wasOpen = open;
        open = !bossAlive && level.shardsCollected() >= required;
        if (open && !wasOpen) {
            level.game().toast("Cổng thời gian đã mở!");
            level.camera().shake(5, 0.3);
        }
        if (open) {
            openPulse += dt;
        }

        Player p = level.player();
        if (p == null || !p.isAlive() || !intersects(p.getX(), p.getY(), p.getWidth(), p.getHeight())) {
            return;
        }

        if (open) {
            level.game().onLevelComplete();
        } else if (bossAlive) {
            level.game().requestPrompt("Hạ gục " + level.boss().displayName() + " trước!");
        } else {
            level.game().requestPrompt("Cần " + required + " Time Shard ("
                    + level.shardsCollected() + "/" + required + ")");
        }
    }

    @Override
    public void render(Graphics2D g) {
        int cx = (int) Math.round(centerX());
        int cy = (int) Math.round(centerY());
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rw = (int) Math.round(width);
        int rh = (int) Math.round(height);

        Color base = open ? new Color(0x6FE3C4) : new Color(0x6B7280);

        // Hai trụ cổng
        g.setColor(new Color(0x1B1F2A));
        g.fillRect(rx, ry, 9, rh);
        g.fillRect(rx + rw - 9, ry, 9, rh);
        g.fillRect(rx, ry, rw, 9);

        // Vùng năng lượng bên trong
        int alpha = open ? (int) (150 + 70 * Math.sin(openPulse * 5)) : 70;
        g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
        g.fillRect(rx + 9, ry + 9, rw - 18, rh - 9);

        // Vòng xoáy
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(3f));
        for (int i = 0; i < 3; i++) {
            double ph = age * 1.6 + i * 2.0;
            int ox = (int) (Math.cos(ph) * 10);
            int oy = (int) (Math.sin(ph) * 16);
            int size = 26 - i * 6;
            g.setColor(new Color(255, 255, 255, open ? 200 : 90));
            g.drawOval(cx - size / 2 + ox, cy - size / 2 + oy, size, size);
        }
        g.setStroke(old);

        if (!open) {
            g.setColor(new Color(255, 255, 255, 200));
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
            String txt = requiredShards + " SHARD";
            int tw = g.getFontMetrics().stringWidth(txt);
            g.drawString(txt, cx - tw / 2, ry - 10);
        }
    }
}
