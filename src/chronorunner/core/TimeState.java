package chronorunner.core;

import java.awt.Color;

/**
 * Ba trạng thái thời gian của trò chơi.
 *
 * <p>Mỗi hằng số mang theo bảng màu riêng, nhờ đó việc đổi thời đại
 * vừa thay đổi logic vừa thay đổi hoàn toàn diện mạo màn chơi.</p>
 *
 * <p>Mỗi trạng thái còn có một "bit" riêng để tạo mặt nạ thời gian
 * (time mask): một vật thể có thể tồn tại ở nhiều thời đại cùng lúc.</p>
 */
public enum TimeState {

    PAST("QUÁ KHỨ",
            new Color(0x2A2312), new Color(0x4A3A1C), new Color(0xE8C069), new Color(0x6B5A34)),

    PRESENT("HIỆN TẠI",
            new Color(0x101C2A), new Color(0x24405A), new Color(0x7FD4FF), new Color(0x44515E)),

    FUTURE("TƯƠNG LAI",
            new Color(0x140C24), new Color(0x2E1148), new Color(0xFF5FD2), new Color(0x53306E));

    /** Bitmask cho từng thời đại. */
    public static final int MASK_PAST = 1;
    public static final int MASK_PRESENT = 2;
    public static final int MASK_FUTURE = 4;
    /** Tồn tại ở mọi thời đại. */
    public static final int ALL = MASK_PAST | MASK_PRESENT | MASK_FUTURE;

    private final String label;
    private final Color skyTop;
    private final Color skyBottom;
    private final Color accent;
    private final Color terrain;

    TimeState(String label, Color skyTop, Color skyBottom, Color accent, Color terrain) {
        this.label = label;
        this.skyTop = skyTop;
        this.skyBottom = skyBottom;
        this.accent = accent;
        this.terrain = terrain;
    }

    /** Bit đại diện cho thời đại này. */
    public int bit() {
        return 1 << ordinal();
    }

    /** Kiểm tra thời đại này có nằm trong mặt nạ hay không. */
    public boolean in(int mask) {
        return (mask & bit()) != 0;
    }

    /** Thời đại kế tiếp theo vòng PAST -> PRESENT -> FUTURE -> PAST. */
    public TimeState next() {
        TimeState[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    /** Thời đại trước đó trong vòng. */
    public TimeState previous() {
        TimeState[] v = values();
        return v[(ordinal() + v.length - 1) % v.length];
    }

    public String label() {
        return label;
    }

    public Color skyTop() {
        return skyTop;
    }

    public Color skyBottom() {
        return skyBottom;
    }

    public Color accent() {
        return accent;
    }

    public Color terrain() {
        return terrain;
    }

    /** Tạo mặt nạ từ danh sách thời đại. */
    public static int maskOf(TimeState... states) {
        int m = 0;
        for (TimeState s : states) {
            m |= s.bit();
        }
        return m;
    }

    /** Lấy an toàn theo chỉ số (dùng khi lưu / nạp game). */
    public static TimeState ofOrdinal(int i) {
        TimeState[] v = values();
        if (i < 0) {
            return v[0];
        }
        return i >= v.length ? v[v.length - 1] : v[i];
    }
}
