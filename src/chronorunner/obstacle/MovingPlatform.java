package chronorunner.obstacle;

import chronorunner.world.Level;

import java.awt.Color;

/**
 * Bệ di chuyển qua lại quanh vị trí gốc theo một trục.
 *
 * <p>Dùng hàm sin nên bệ tăng tốc và giảm tốc mượt ở hai đầu,
 * tránh cảm giác giật khi đổi hướng.</p>
 */
public class MovingPlatform extends Platform {

    private final double originX;
    private final double originY;

    private final double dirX;
    private final double dirY;
    private final double range;
    private final double speed;

    private double phase;

    /**
     * @param dirX  hướng di chuyển trục X (-1, 0 hoặc 1)
     * @param dirY  hướng di chuyển trục Y (-1, 0 hoặc 1)
     * @param range quãng đường di chuyển mỗi bên (pixel)
     * @param speed tốc độ góc
     */
    public MovingPlatform(double x, double y, double w, double h,
                          double dirX, double dirY, double range, double speed) {
        super(x, y, w, h);
        this.originX = x;
        this.originY = y;
        double len = Math.hypot(dirX, dirY);
        this.dirX = len == 0 ? 0 : dirX / len;
        this.dirY = len == 0 ? 0 : dirY / len;
        this.range = range;
        this.speed = speed;
        this.oneWay = true;
        this.bodyColor = new Color(0x3E6B63);
        this.edgeColor = new Color(0x6FE3C4);
    }

    /** Bệ di chuyển ngang. */
    public static MovingPlatform horizontal(double x, double y, double w, double h,
                                            double range, double speed) {
        return new MovingPlatform(x, y, w, h, 1, 0, range, speed);
    }

    /** Bệ di chuyển dọc. */
    public static MovingPlatform vertical(double x, double y, double w, double h,
                                          double range, double speed) {
        return new MovingPlatform(x, y, w, h, 0, 1, range, speed);
    }

    @Override
    protected void move(Level level, double dt) {
        phase += dt * speed;
        double offset = Math.sin(phase) * range;
        x = originX + dirX * offset;
        y = originY + dirY * offset;
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        super.render(g);
        if (!temporalActive) {
            return;
        }
        // Mũi tên nhỏ chỉ hướng di chuyển
        g.setColor(new Color(255, 255, 255, 130));
        int cx = (int) Math.round(centerX());
        int cy = (int) Math.round(centerY());
        if (dirX != 0) {
            g.fillPolygon(new int[]{cx - 6, cx + 6, cx}, new int[]{cy - 4, cy - 4, cy + 5}, 3);
        } else {
            g.fillPolygon(new int[]{cx - 4, cx - 4, cx + 5}, new int[]{cy - 6, cy + 6, cy}, 3);
        }
    }
}
