package chronorunner.obstacle;

import chronorunner.core.TimeState;
import chronorunner.entity.Character;
import chronorunner.world.Level;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Lớp cơ sở cho mọi loại bệ đỡ.
 *
 * <p>Điểm chung của các bệ: chúng có thể di chuyển, và khi di chuyển thì
 * nhân vật đứng trên phải được "chở" theo. Cơ chế đó nằm ở
 * {@link #deltaX()} / {@link #deltaY()} - quãng đường bệ đã đi trong bước
 * cập nhật vừa rồi.</p>
 */
public abstract class Platform extends Obstacle {

    protected boolean oneWay = true;
    protected double deltaX;
    protected double deltaY;

    /** Bệ còn hiệu lực ở thời đại hiện tại hay không. */
    protected boolean temporalActive = true;

    protected Color bodyColor = new Color(0x54606E);
    protected Color edgeColor = new Color(0x7FD4FF);

    protected Platform(double x, double y, double w, double h) {
        super(x, y, w, h);
        this.solid = true;
        this.timeMask = TimeState.ALL;
    }

    // ---- Thuộc tính --------------------------------------------------------
    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public double deltaX() {
        return deltaX;
    }

    public double deltaY() {
        return deltaY;
    }

    /** Xoá quãng đường di chuyển (dùng khi thời gian bị đóng băng). */
    public void clearDelta() {
        deltaX = 0;
        deltaY = 0;
    }

    public boolean isTemporalActive() {
        return temporalActive;
    }

    /** Bệ này có chặn nhân vật {@code c} hay không. */
    public boolean blocks(Character c) {
        return alive && visible && temporalActive;
    }

    /** Hook khi có nhân vật đáp xuống bệ. */
    public void onStand(Character c) {
    }

    // ---- Cập nhật ----------------------------------------------------------
    @Override
    public void update(Level level, double dt) {
        super.update(level, dt);
        double beforeX = x;
        double beforeY = y;
        move(level, dt);
        deltaX = x - beforeX;
        deltaY = y - beforeY;
    }

    /** Hành vi di chuyển riêng của từng loại bệ. */
    protected abstract void move(Level level, double dt);

    @Override
    public void onTimeChanged(TimeState from, TimeState to) {
        temporalActive = existsIn(to);
    }

    /** Đặt lại vị trí gốc (dùng cho bệ rơi hồi sinh). */
    protected void moveTo(double nx, double ny) {
        this.x = nx;
        this.y = ny;
    }

    // ---- Vẽ ----------------------------------------------------------------
    @Override
    public void render(Graphics2D g) {
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rw = (int) Math.round(width);
        int rh = (int) Math.round(height);

        if (!temporalActive) {
            // Bệ đã "biến mất" ở thời đại này: chỉ vẽ viền mờ làm manh mối.
            g.setColor(new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 55));
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{6f, 8f}, 0f));
            g.drawRect(rx, ry, rw, rh);
            g.setStroke(old);
            return;
        }

        g.setColor(bodyColor);
        g.fillRect(rx, ry, rw, rh);

        g.setColor(new Color(0, 0, 0, 70));
        g.fillRect(rx, ry + rh - 5, rw, 5);

        g.setColor(edgeColor);
        g.fillRect(rx, ry, rw, 4);

        g.setColor(new Color(0, 0, 0, 90));
        g.drawRect(rx, ry, rw - 1, rh - 1);
    }
}
