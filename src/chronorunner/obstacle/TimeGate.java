package chronorunner.obstacle;

import chronorunner.core.TimeState;
import chronorunner.entity.Particle;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Cổng thời gian - nơi duy nhất người chơi có thể đổi thời đại.
 *
 * <p>Nhấn E khi đứng trong cổng để đi theo vòng
 * QUÁ KHỨ -> HIỆN TẠI -> TƯƠNG LAI -> QUÁ KHỨ.</p>
 */
public class TimeGate extends Obstacle {

    private static final double SIZE_W = 44;
    private static final double SIZE_H = 76;

    private double cooldown;
    private boolean playerInside;

    public TimeGate(double x, double y) {
        super(x - SIZE_W / 2, y - SIZE_H, SIZE_W, SIZE_H);
        this.timeMask = TimeState.ALL;
        this.solid = false;
    }

    public boolean isPlayerInside() {
        return playerInside;
    }

    @Override
    public void update(Level level, double dt) {
        super.update(level, dt);
        if (cooldown > 0) {
            cooldown -= dt;
        }

        Player p = level.player();
        playerInside = p != null && p.isAlive()
                && intersects(p.getX() - 6, p.getY(), p.getWidth() + 12, p.getHeight());

        if (playerInside) {
            level.game().requestPrompt("[E] Dịch chuyển thời gian");
            if (cooldown <= 0 && level.input().interactPressed()) {
                shift(level);
            }
        }
    }

    private void shift(Level level) {
        TimeState from = level.timeState();
        TimeState to = from.next();
        level.shiftTime(to);
        cooldown = 0.4;

        level.game().toast("Dòng thời gian: " + to.label());
        level.camera().shake(4, 0.22);

        // Hiệu ứng hạt toả ra từ tâm cổng
        for (int i = 0; i < 26; i++) {
            double ang = Math.PI * 2 * i / 26.0;
            level.spawn(new Particle(centerX(), centerY(),
                    Math.cos(ang) * 130, Math.sin(ang) * 130,
                    to.accent(), 0.55));
        }
    }

    @Override
    public void render(Graphics2D g) {
        int cx = (int) Math.round(centerX());
        int cy = (int) Math.round(centerY());
        int rw = (int) Math.round(width);
        int rh = (int) Math.round(height);

        TimeState[] states = TimeState.values();
        // Vẽ 3 vòng xoay, mỗi vòng ứng với một thời đại
        for (int i = 0; i < states.length; i++) {
            TimeState s = states[i];
            double phase = age * (0.8 + i * 0.35) + i * 2.1;
            int rx = (int) (Math.cos(phase) * 13);
            int ry = (int) (Math.sin(phase * 1.3) * 7);
            int size = 30 - i * 4;
            g.setColor(new Color(s.accent().getRed(), s.accent().getGreen(), s.accent().getBlue(), 190));
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(3f));
            g.drawOval(cx - size / 2 + rx, cy - size / 2 + ry, size, size);
            g.setStroke(old);
        }

        // Khung cổng
        g.setColor(new Color(0x1B1F2A));
        g.fillRect((int) Math.round(x), (int) Math.round(y), 6, rh);
        g.fillRect((int) Math.round(x + width - 6), (int) Math.round(y), 6, rh);
        g.fillRect((int) Math.round(x), (int) Math.round(y), rw, 6);

        // Ánh sáng nền
        g.setColor(new Color(255, 255, 255, playerInside ? 46 : 22));
        g.fillRect((int) Math.round(x) + 6, (int) Math.round(y) + 6, rw - 12, rh - 6);

        if (playerInside) {
            g.setColor(new Color(255, 255, 255, 210));
            String hint = "E";
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 15f));
            int tw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, cx - tw / 2, (int) Math.round(y) - 8);
        }
    }
}
