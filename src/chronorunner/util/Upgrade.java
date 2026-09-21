package chronorunner.util;

/**
 * Các nâng cấp của Chrono Core.
 *
 * <p>Mỗi hằng số tự mô tả tên, mô tả, số cấp tối đa và giá của từng cấp.
 * Nhờ vậy cửa hàng nâng cấp chỉ cần duyệt qua {@code Upgrade.values()}
 * mà không cần bất kỳ câu lệnh {@code if} đặc biệt nào.</p>
 */
public enum Upgrade {

    REWIND_DURATION("Tua Ngược", "Kéo dài thời gian tua ngược thời gian", 3, new int[]{60, 140, 260}),

    TIME_ENERGY("Năng Lượng", "Tăng năng lượng thời gian tối đa", 3, new int[]{40, 100, 180}),

    DASH("Dash", "Mở khoá lướt nhanh (Shift)", 2, new int[]{80, 180}),

    DOUBLE_JUMP("Nhảy Đôi", "Mở khoá nhảy lần thứ hai", 1, new int[]{120}),

    TIME_FREEZE("Đóng Băng", "Mở khoá đóng băng thời gian (F)", 2, new int[]{140, 240});

    private final String displayName;
    private final String description;
    private final int maxLevel;
    private final int[] costs;

    Upgrade(String displayName, String description, int maxLevel, int[] costs) {
        this.displayName = displayName;
        this.description = description;
        this.maxLevel = maxLevel;
        this.costs = costs;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public int maxLevel() {
        return maxLevel;
    }

    /** Giá để nâng từ cấp {@code level} lên {@code level + 1}. */
    public int costAt(int level) {
        if (level < 0) {
            level = 0;
        }
        return level >= costs.length ? Integer.MAX_VALUE : costs[level];
    }

    /** Trạng thái hiển thị của nâng cấp tại một cấp độ. */
    public static String levelText(int level, int maxLevel) {
        if (level <= 0 && maxLevel > 0) {
            return "CHƯA MỞ";
        }
        return "Cấp " + level + "/" + maxLevel;
    }
}
