package chronorunner.util;

/**
 * Hình chữ nhật mutable dùng cho hitbox.
 * Được tái sử dụng (không cấp phát mới mỗi khung hình) để giảm rác bộ nhớ.
 */
public class Rect {

    public double x;
    public double y;
    public double w;
    public double h;

    public Rect() {
        this(0, 0, 0, 0);
    }

    public Rect(double x, double y, double w, double h) {
        set(x, y, w, h);
    }

    public Rect set(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        return this;
    }

    public Rect set(Rect o) {
        return set(o.x, o.y, o.w, o.h);
    }

    /** Dời hình chữ nhật theo vector (dx, dy). */
    public Rect translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        return this;
    }

    public double right() {
        return x + w;
    }

    public double bottom() {
        return y + h;
    }

    public double centerX() {
        return x + w * 0.5;
    }

    public double centerY() {
        return y + h * 0.5;
    }

    public boolean intersects(Rect o) {
        return Collision.overlap(this, o);
    }

    public boolean intersects(double bx, double by, double bw, double bh) {
        return Collision.overlap(x, y, w, h, bx, by, bw, bh);
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    @Override
    public String toString() {
        return String.format("Rect(%.1f, %.1f, %.1f, %.1f)", x, y, w, h);
    }
}
