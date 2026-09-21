package chronorunner.util;

/**
 * Tiện ích toán học / va chạm dùng chung cho toàn bộ game.
 * Lớp tiện ích thuần static nên không cho phép khởi tạo.
 */
public final class Collision {

    private Collision() {
    }

    /** Kiểm tra giao nhau của 2 hình chữ nhật AABB. */
    public static boolean overlap(double ax, double ay, double aw, double ah,
                                  double bx, double by, double bw, double bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    public static boolean overlap(Rect a, Rect b) {
        return overlap(a.x, a.y, a.w, a.h, b.x, b.y, b.w, b.h);
    }

    /** Giao nhau có "độ trễ" (epsilon) - dùng để tha thứ va chạm sát mép. */
    public static boolean overlapEps(double ax, double ay, double aw, double ah,
                                     double bx, double by, double bw, double bh, double eps) {
        return ax < bx + bw - eps && ax + aw > bx + eps && ay < by + bh - eps && ay + ah > by + eps;
    }

    public static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    public static int clampInt(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Tiến giá trị `cur` về `target` một khoảng tối đa `maxDelta`. */
    public static double approach(double cur, double target, double maxDelta) {
        if (cur < target) {
            return Math.min(cur + maxDelta, target);
        }
        return Math.max(cur - maxDelta, target);
    }

    public static int sign(double v) {
        return v > 0 ? 1 : (v < 0 ? -1 : 0);
    }

    public static double dist(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double dist2(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    /** Chuyển giây -> số khung hình ở 60 FPS. */
    public static int secondsToTicks(double seconds) {
        return (int) Math.round(seconds * 60.0);
    }
}
