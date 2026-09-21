package chronorunner.obstacle;

import chronorunner.core.TimeState;
import chronorunner.entity.Player;
import chronorunner.world.Level;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Bệ thời gian: chỉ tồn tại ở những thời đại nằm trong mặt nạ của nó.
 *
 * <p>Đây là cơ chế lõi của phần puzzle: người chơi phải dịch chuyển thời đại
 * đúng lúc để tạo ra đường đi.</p>
 */
public class TemporalPlatform extends Platform {

    public TemporalPlatform(double x, double y, double w, double h, int timeMask) {
        super(x, y, w, h);
        this.timeMask = timeMask;
        this.oneWay = false;
        applyTheme(timeMask);
    }

    private void applyTheme(int mask) {
        if (mask == TimeState.MASK_PAST) {
            bodyColor = new Color(0x7C6330);
            edgeColor = new Color(0xE8C069);
        } else if (mask == TimeState.MASK_FUTURE) {
            bodyColor = new Color(0x5B2E7E);
            edgeColor = new Color(0xFF5FD2);
        } else if (mask == (TimeState.MASK_PAST | TimeState.MASK_FUTURE)) {
            bodyColor = new Color(0x6A4A6E);
            edgeColor = new Color(0xC08CF0);
        } else {
            bodyColor = new Color(0x3F5A6B);
            edgeColor = new Color(0x7FD4FF);
        }
    }

    @Override
    protected void move(Level level, double dt) {
        // Bệ thời gian đứng yên, chỉ thay đổi tính hiện hữu.
    }

    @Override
    public void render(Graphics2D g) {
        if (temporalActive) {
            super.render(g);
            // Vầng sáng nhấp nháy cho biết đây là bệ "có thật" ở thời đại này
            int pulse = (int) (40 + 30 * Math.sin(age * 4));
            g.setColor(new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), pulse));
            g.fillRect((int) Math.round(x) - 2, (int) Math.round(y) - 2,
                    (int) Math.round(width) + 4, (int) Math.round(height) + 4);
            return;
        }

        // Không tồn tại: vẽ khung nét đứt để người chơi biết chỗ này có gì đó.
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{7f, 7f}, (float) (age * 18) % 14f));
        g.setColor(new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 120));
        g.drawRect((int) Math.round(x), (int) Math.round(y),
                (int) Math.round(width), (int) Math.round(height));
        g.setStroke(old);
    }

    /** Mô tả ngắn để HUD hiển thị khi người chơi đứng gần. */
    public String describe() {
        StringBuilder sb = new StringBuilder("Chỉ tồn tại ở: ");
        for (TimeState s : TimeState.values()) {
            if (existsIn(s)) {
                sb.append(s.label()).append(' ');
            }
        }
        return sb.toString().trim();
    }

    /** Người chơi có đang đứng trên bệ này không. */
    public boolean isSupporting(Player p) {
        return p != null && p.standingPlatform() == this;
    }
}
