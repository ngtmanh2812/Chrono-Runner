package chronorunner.core;

import java.awt.Graphics2D;

/**
 * Một màn hình (screen) của trò chơi: menu, màn chơi, cửa hàng nâng cấp...
 *
 * <p>{@link Game} quản lý một ngăn xếp các Screen. Chỉ Screen trên cùng được
 * cập nhật, nhưng mọi Screen trong ngăn xếp đều được vẽ - nhờ đó lớp phủ
 * (pause, game over) vẫn thấy được khung cảnh phía dưới.</p>
 */
public interface Screen {

    /** Gọi một lần khi Screen được đưa lên đỉnh ngăn xếp. */
    default void onEnter(Game game) {
    }

    /** Gọi một lần khi Screen bị lấy ra khỏi ngăn xếp. */
    default void onExit(Game game) {
    }

    /**
     * Gọi khi lớp phủ nằm trên Screen này bị đóng, tức là Screen
     * được "thức dậy" và trở lại nhận cập nhật.
     */
    default void onResume(Game game) {
    }

    /** Cập nhật logic. Chỉ được gọi cho Screen trên cùng. */
    void update(Game game, double dt);

    /** Vẽ nội dung lên bộ đệm đồ hoạ. */
    void render(Game game, Graphics2D g);

    /**
     * Nếu trả về {@code true}, các Screen nằm dưới vẫn được vẽ
     * (dùng cho các lớp phủ tạm dừng / kết thúc màn).
     */
    default boolean transparent() {
        return false;
    }

    /** Tên hiển thị, phục vụ gỡ lỗi. */
    default String name() {
        return getClass().getSimpleName();
    }
}
