package chronorunner.entity;

import chronorunner.core.TimeState;
import chronorunner.util.Collision;
import chronorunner.util.Rect;
import chronorunner.world.Level;

import java.awt.Graphics2D;

/**
 * Gốc của toàn bộ cây kế thừa trong trò chơi.
 *
 * <pre>
 *                    GameObject
 *          ┌──────────────┼───────────────┐
 *      Character        Item          Obstacle
 *      ┌───┴────┐    ┌────┴────┐     ┌────┴─────┐
 *   Player   Enemy  TimeShard ...  Platform   Trap
 * </pre>
 *
 * <p>Mọi vật thể đều có: vị trí, vận tốc, kích thước, hitbox, cờ "còn sống"
 * và <b>mặt nạ thời gian</b> ({@link #timeMask}) quyết định nó tồn tại ở
 * thời đại nào.</p>
 */
public abstract class GameObject {

    protected double x;
    protected double y;
    protected double width;
    protected double height;

    protected double velX;
    protected double velY;

    protected boolean alive = true;
    protected boolean visible = true;
    protected boolean solid = false;

    /** Mặt nạ thời gian: vật thể chỉ tồn tại ở các thời đại có bit tương ứng. */
    protected int timeMask = TimeState.ALL;

    /** Tuổi thọ (giây) - dùng cho hiệu ứng và animation. */
    protected double age;

    private final Rect cachedBounds = new Rect();

    protected GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Cập nhật logic cho một bước vật lý.
     *
     * @param level màn chơi hiện tại, cung cấp truy vấn va chạm và khả năng sinh vật thể mới
     * @param dt    bước thời gian cố định (giây)
     */
    public abstract void update(Level level, double dt);

    /** Vẽ vật thể. Gốc toạ độ đã được camera dịch trước khi gọi. */
    public abstract void render(Graphics2D g);

    // ---- Hình học ----------------------------------------------------------
    /** Hitbox hiện tại (đối tượng được tái sử dụng, không cấp phát mới). */
    public Rect bounds() {
        return cachedBounds.set(x, y, width, height);
    }

    public double centerX() {
        return x + width * 0.5;
    }

    public double centerY() {
        return y + height * 0.5;
    }

    public double left() {
        return x;
    }

    public double right() {
        return x + width;
    }

    public double top() {
        return y;
    }

    public double bottom() {
        return y + height;
    }

    public boolean intersects(GameObject other) {
        return Collision.overlap(x, y, width, height,
                other.x, other.y, other.width, other.height);
    }

    public boolean intersects(double ox, double oy, double ow, double oh) {
        return Collision.overlap(x, y, width, height, ox, oy, ow, oh);
    }

    public double distanceTo(GameObject other) {
        return Collision.dist(centerX(), centerY(), other.centerX(), other.centerY());
    }

    // ---- Thời gian ---------------------------------------------------------
    /** Vật thể này có tồn tại ở thời đại đang xét hay không. */
    public boolean existsIn(TimeState state) {
        return state.in(timeMask);
    }

    /**
     * Hook được gọi khi thời đại thay đổi. Mặc định không làm gì;
     * lớp con ghi đè để bật/tắt hoặc đổi trạng thái.
     */
    public void onTimeChanged(TimeState from, TimeState to) {
    }

    // ---- Vòng đời ----------------------------------------------------------
    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSolid() {
        return solid;
    }

    public double age() {
        return age;
    }

    /** Khoảng cách tới tâm màn hình theo trục X, dùng để lọc vật thể ngoài khung hình. */
    public boolean isNear(double cx, double cy, double radius) {
        return Math.abs(centerX() - cx) < radius && Math.abs(centerY() - cy) < radius;
    }

    // ---- Truy cập / sửa đổi ------------------------------------------------
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(double w, double h) {
        this.width = w;
        this.height = h;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public void setVelocity(double vx, double vy) {
        this.velX = vx;
        this.velY = vy;
    }

    public int getTimeMask() {
        return timeMask;
    }

    public void setTimeMask(int mask) {
        this.timeMask = mask;
    }

    @Override
    public String toString() {
        return String.format("%s[x=%.1f y=%.1f w=%.1f h=%.1f]",
                getClass().getSimpleName(), x, y, width, height);
    }
}
