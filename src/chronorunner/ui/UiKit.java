package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.InputHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Bộ công cụ vẽ giao diện dùng chung cho mọi màn hình.
 *
 * <p>Tập trung tại đây bảng màu, hệ thống phông chữ và các hàm vẽ khối
 * (khung, nút, thanh tiến trình) để các lớp Screen chỉ còn tập trung vào
 * bố cục và luồng xử lý.</p>
 */
public final class UiKit {

    // ---- Bảng màu ----------------------------------------------------------
    public static final Color BACKDROP = new Color(0x070A11);
    public static final Color PANEL = new Color(0x131A29);
    public static final Color PANEL_LIGHT = new Color(0x1C2740);
    public static final Color EDGE = new Color(0x2E405C);
    public static final Color EDGE_SOFT = new Color(0x22304A);

    public static final Color TEXT = new Color(0xE8F0FA);
    public static final Color TEXT_DIM = new Color(0x93A6BF);
    public static final Color TEXT_MUTED = new Color(0x5F6E85);

    public static final Color ACCENT = new Color(0x7FD4FF);
    public static final Color ACCENT_DEEP = new Color(0x3C7FA8);
    public static final Color GOLD = new Color(0xFFD166);
    public static final Color SHARD = new Color(0xFF5FD2);
    public static final Color DANGER = new Color(0xE8574A);
    public static final Color OK = new Color(0x6FE3C4);
    public static final Color VIOLET = new Color(0xC08CF0);

    private static final String FAMILY = Font.SANS_SERIF;
    private static final Map<Integer, Font> FONTS = new HashMap<>();

    private UiKit() {
    }

    /** Phông chữ được lưu đệm để không phải cấp phát lại mỗi khung hình. */
    public static Font font(int style, int size) {
        int key = (style << 12) | size;
        Font f = FONTS.get(key);
        if (f == null) {
            f = new Font(FAMILY, style, size);
            FONTS.put(key, f);
        }
        return f;
    }

    public static Font regular(int size) {
        return font(Font.PLAIN, size);
    }

    public static Font bold(int size) {
        return font(Font.BOLD, size);
    }

    /** Bật khử răng cưa cho toàn bộ bộ đệm đồ hoạ. */
    public static void prepare(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ---- Hình khối ---------------------------------------------------------
    public static Shape roundRect(double x, double y, double w, double h, double radius) {
        return new RoundRectangle2D.Double(x, y, w, h, radius, radius);
    }

    /** Khung nền có viền, kiểu "thẻ" dùng cho panel và nút. */
    public static void panel(Graphics2D g, double x, double y, double w, double h,
                             Color fill, Color edge, double radius) {
        g.setColor(fill);
        g.fill(roundRect(x, y, w, h, radius));
        g.setColor(edge);
        g.setStroke(new BasicStroke(1.6f));
        g.draw(roundRect(x + 0.8, y + 0.8, w - 1.6, h - 1.6, radius));
    }

    public static void panel(Graphics2D g, double x, double y, double w, double h) {
        panel(g, x, y, w, h, new Color(0x131A29), EDGE, 14);
    }

    /** Nền chuyển sắc dọc, dùng cho màn hình menu. */
    public static void verticalGradient(Graphics2D g, int w, int h, Color top, Color bottom) {
        g.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 1f},
                new Color[]{top, bottom}));
        g.fillRect(0, 0, w, h);
    }

    /** Lớp phủ tối dần ở bốn góc, tạo chiều sâu cho màn hình. */
    public static void vignette(Graphics2D g, int w, int h) {
        for (int i = 0; i < 90; i++) {
            int alpha = (int) (2 + i * 0.55);
            g.setColor(new Color(0, 0, 0, Math.min(120, alpha)));
            g.drawRect(i, i, w - i * 2 - 1, h - i * 2 - 1);
        }
    }

    /** Lớp phủ mờ dùng cho màn tạm dừng / hết màn. */
    public static void scrim(Graphics2D g, int w, int h, int alpha) {
        g.setColor(new Color(4, 6, 11, alpha));
        g.fillRect(0, 0, w, h);
    }

    // ---- Chữ ---------------------------------------------------------------
    public static void text(Graphics2D g, String s, double x, double baselineY,
                            Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(s, (float) x, (float) baselineY);
    }

    public static void textCenter(Graphics2D g, String s, double centerX, double baselineY,
                                  Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        int w = g.getFontMetrics().stringWidth(s);
        g.drawString(s, (float) (centerX - w / 2.0), (float) baselineY);
    }

    public static void textRight(Graphics2D g, String s, double rightX, double baselineY,
                                 Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        int w = g.getFontMetrics().stringWidth(s);
        g.drawString(s, (float) (rightX - w), (float) baselineY);
    }

    /** Chữ có bóng đổ, dùng cho tiêu đề lớn. */
    public static void textCenterShadow(Graphics2D g, String s, double centerX, double baselineY,
                                        Font font, Color color, int spread) {
        g.setFont(font);
        int w = g.getFontMetrics().stringWidth(s);
        g.setColor(new Color(0, 0, 0, 150));
        for (int dx = -spread; dx <= spread; dx += spread) {
            for (int dy = -spread; dy <= spread; dy += spread) {
                g.drawString(s, (float) (centerX - w / 2.0 + dx), (float) (baselineY + dy));
            }
        }
        g.setColor(color);
        g.drawString(s, (float) (centerX - w / 2.0), (float) baselineY);
    }

    /** Ngắt một đoạn văn dài thành nhiều dòng vừa bề rộng cho trước. */
    public static java.util.List<String> wrap(Graphics2D g, String text, Font font, int maxWidth) {
        g.setFont(font);
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (g.getFontMetrics().stringWidth(candidate) > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }

    // ---- Thanh tiến trình --------------------------------------------------
    public static void bar(Graphics2D g, double x, double y, double w, double h,
                           double ratio, Color fill, Color back, Color edge) {
        double r = Math.max(0, Math.min(1, ratio));
        g.setColor(back);
        g.fill(roundRect(x, y, w, h, h / 2));
        if (r > 0) {
            g.setColor(fill);
            g.fill(roundRect(x, y, Math.max(h, w * r), h, h / 2));
        }
        g.setColor(edge);
        g.setStroke(new BasicStroke(1.4f));
        g.draw(roundRect(x + 0.7, y + 0.7, w - 1.4, h - 1.4, h / 2));
    }

    /** Biểu tượng Time Shard nhỏ (hai hình thoi lồng nhau). */
    public static void shardIcon(Graphics2D g, double cx, double cy, double size, Color color) {
        g.setColor(color);
        java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
        p.moveTo(cx, cy - size);
        p.lineTo(cx + size * 0.62, cy);
        p.lineTo(cx, cy + size);
        p.lineTo(cx - size * 0.62, cy);
        p.closePath();
        g.fill(p);
        g.setColor(new Color(255, 255, 255, 170));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(p);
    }

    /** Biểu tượng đồng coin. */
    public static void coinIcon(Graphics2D g, double cx, double cy, double radius) {
        g.setColor(new Color(0xB8860B));
        g.fillOval((int) (cx - radius), (int) (cy - radius), (int) (radius * 2), (int) (radius * 2));
        g.setColor(GOLD);
        g.fillOval((int) (cx - radius + 2), (int) (cy - radius + 2),
                (int) (radius * 2 - 4), (int) (radius * 2 - 4));
        g.setColor(new Color(0x6B4A08));
        g.setFont(bold((int) Math.max(8, radius)));
        String s = "C";
        int w = g.getFontMetrics().stringWidth(s);
        g.drawString(s, (float) (cx - w / 2.0), (float) (cy + radius * 0.42));
    }

    /** Trái tim nhỏ biểu thị máu. */
    public static void heartIcon(Graphics2D g, double cx, double cy, double size, boolean filled) {
        java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
        double s = size;
        p.moveTo(cx, cy + s * 0.75);
        p.curveTo(cx - s * 1.4, cy - s * 0.25, cx - s * 0.45, cy - s * 1.05, cx, cy - s * 0.3);
        p.curveTo(cx + s * 0.45, cy - s * 1.05, cx + s * 1.4, cy - s * 0.25, cx, cy + s * 0.75);
        p.closePath();
        g.setColor(filled ? DANGER : new Color(0x3A2430));
        g.fill(p);
        g.setColor(filled ? new Color(255, 150, 140) : new Color(0x54404A));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(p);
    }

    // ---- Nút bấm -----------------------------------------------------------
    /** Một nút bấm hình chữ nhật, tự xử lý hover và nhấn chuột. */
    public static final class Button {

        private final double x;
        private final double y;
        private final double w;
        private final double h;

        private String label;
        private String hint;
        private boolean enabled = true;
        private boolean hovered;
        private Runnable action;

        private Button(String label, double x, double y, double w, double h, Runnable action) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.action = action;
        }

        /** Đổi nhãn nút (dùng khi trạng thái thay đổi, ví dụ "MUA" -> "TỐI ĐA"). */
        public void setLabel(String label) {
            this.label = label;
        }

        public Button hint(String hint) {
            this.hint = hint;
            return this;
        }

        public Button disabled() {
            this.enabled = false;
            return this;
        }

        public Button action(Runnable action) {
            this.action = action;
            return this;
        }

        /** Kích hoạt nút theo cách thủ công (bàn phím, tự động...). */
        public void trigger() {
            if (enabled && action != null) {
                action.run();
            }
        }

        public boolean enabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean hovered() {
            return hovered && enabled;
        }

        public double centerX() {
            return x + w / 2;
        }

        public double centerY() {
            return y + h / 2;
        }

        public boolean contains(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    public static Button button(String label, double x, double y, double w, double h) {
        return new Button(label, x, y, w, h, null);
    }

    /**
     * Cập nhật trạng thái hover và kích hoạt nút nếu được bấm.
     *
     * @return {@code true} nếu nút vừa được kích hoạt
     */
    public static boolean update(Button b, InputHandler input) {
        b.hovered = b.contains(input.mouseX(), input.mouseY());
        if (!b.enabled) {
            return false;
        }
        if (b.hovered && input.mouseJustPressed()) {
            if (b.action != null) {
                b.action.run();
            }
            return true;
        }
        return false;
    }

    /** Vẽ nút với tông màu chủ đạo cho trước. */
    public static void draw(Graphics2D g, Button b, Color accent) {
        draw(g, b, accent, null);
    }

    /**
     * Vẽ nút.
     *
     * @param accent  màu nhấn khi nút đang được trỏ tới
     * @param override màu chữ thay thế (có thể {@code null})
     */
    public static void draw(Graphics2D g, Button b, Color accent, Color override) {
        boolean hot = b.hovered();
        Color fill = b.enabled
                ? (hot ? blend(PANEL_LIGHT, accent, 0.22) : PANEL)
                : new Color(0x11151E);
        Color edge = b.enabled ? (hot ? accent : EDGE) : new Color(0x212833);
        panel(g, b.x, b.y, b.w, b.h, fill, edge, 12);

        if (hot) {
            g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 26));
            g.fill(roundRect(b.x + 2, b.y + 2, b.w - 4, b.h - 4, 10));
        }

        Color textColor = override != null ? override
                : (b.enabled ? (hot ? Color.WHITE : TEXT) : TEXT_MUTED);
        Font f = bold(22);
        textCenter(g, b.label, b.centerX(), b.centerY() + 8, f, textColor);

        if (hot && b.hint != null) {
            textCenter(g, b.hint, b.centerX(), b.y + b.h + 22, regular(15), TEXT_DIM);
        }
    }

    /** Trộn hai màu theo tỉ lệ {@code t} (0 = a, 1 = b). */
    public static Color blend(Color a, Color b, double t) {
        double k = Math.max(0, Math.min(1, t));
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * k),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * k));
    }

    public static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    /** Vẽ vành đồng hồ xoay - hoạ tiết chủ đạo của trò chơi. */
    public static void clockRing(Graphics2D g, double cx, double cy, double radius,
                                 double phase, Color color, float thickness) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(alpha(color, 110));
        g.drawOval((int) (cx - radius), (int) (cy - radius), (int) (radius * 2), (int) (radius * 2));
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6 + phase;
            double inner = radius - 8;
            g.drawLine((int) (cx + Math.cos(a) * inner), (int) (cy + Math.sin(a) * inner),
                    (int) (cx + Math.cos(a) * radius), (int) (cy + Math.sin(a) * radius));
        }
        g.setColor(alpha(color, 220));
        double ha = phase * 2.2;
        g.drawLine((int) cx, (int) cy,
                (int) (cx + Math.cos(ha) * radius * 0.6), (int) (cy + Math.sin(ha) * radius * 0.6));
        g.drawLine((int) cx, (int) cy,
                (int) (cx + Math.cos(ha * 5) * radius * 0.38),
                (int) (cy + Math.sin(ha * 5) * radius * 0.38));
        g.setStroke(old);
    }

    /** Nền sao trôi chậm cho các màn hình menu. */
    public static void starfield(Graphics2D g, int w, int h, double time, Color tint) {
        for (int i = 0; i < 90; i++) {
            double sx = (i * 179.3 + time * (7 + i % 5)) % w;
            double sy = (i * 97.7) % h;
            int size = (i % 7 == 0) ? 3 : 2;
            g.setColor(alpha(tint, 20 + (i * 13) % 40));
            g.fillRect((int) sx, (int) sy, size, size);
        }
    }

    /** Định dạng thời gian mm:ss. */
    public static String formatTime(double seconds) {
        int total = (int) Math.max(0, seconds);
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    /** Bảng chữ cái có dấu dùng chung cho phần hướng dẫn. */
    public static void drawKey(Graphics2D g, String key, double x, double y) {
        g.setFont(bold(15));
        int w = Math.max(30, g.getFontMetrics().stringWidth(key) + 16);
        panel(g, x, y, w, 26, PANEL_LIGHT, EDGE, 7);
        textCenter(g, key, x + w / 2.0, y + 18, bold(15), TEXT);
    }

    /** Kích thước màn hình nội bộ, tiện cho việc tính bố cục. */
    public static int viewWidth() {
        return Game.VIEW_W;
    }

    public static int viewHeight() {
        return Game.VIEW_H;
    }
}
