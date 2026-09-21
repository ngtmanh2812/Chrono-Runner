package chronorunner.world;

import chronorunner.core.TimeState;

import java.awt.Color;

/**
 * Các loại ô của bản đồ dạng lưới.
 *
 * <p>Mỗi loại ô tự khai báo: có rắn không, tồn tại ở thời đại nào,
 * có gây sát thương không. Nhờ đó phần vật lý chỉ cần hỏi
 * {@link #solidIn(TimeState)} mà không cần biết ô đó là gì.</p>
 */
public enum TileType {

    /** Ô trống. */
    EMPTY(' ', false, TimeState.ALL, false),

    /** Nền đất rắn, tồn tại ở mọi thời đại. */
    GROUND('#', true, TimeState.ALL, false),

    /** Cầu nối: còn nguyên ở QUÁ KHỨ và TƯƠNG LAI, đứt gãy ở HIỆN TẠI. */
    BRIDGE('X', true, TimeState.MASK_PAST | TimeState.MASK_FUTURE, false),

    /** Bệ đá chỉ tồn tại ở QUÁ KHỨ. */
    PLATFORM_PAST('P', true, TimeState.MASK_PAST, false),

    /** Bệ năng lượng chỉ tồn tại ở TƯƠNG LAI. */
    PLATFORM_FUTURE('U', true, TimeState.MASK_FUTURE, false),

    /** Băng trơn (màn 4): rắn nhưng giảm ma sát. */
    ICE('=', true, TimeState.ALL, false),

    /** Bãi gai: không rắn, gây sát thương. */
    SPIKE('^', false, TimeState.ALL, true),

    /** Dung nham: không rắn, gây sát thương, hất người chơi lên. */
    LAVA('L', false, TimeState.ALL, true),

    /** Khối trang trí nền: không va chạm, chỉ để vẽ. */
    DECOR('b', false, TimeState.ALL, false);

    private final char symbol;
    private final boolean solid;
    private final int timeMask;
    private final boolean hazard;

    TileType(char symbol, boolean solid, int timeMask, boolean hazard) {
        this.symbol = symbol;
        this.solid = solid;
        this.timeMask = timeMask;
        this.hazard = hazard;
    }

    public char symbol() {
        return symbol;
    }

    /** Ô này có chặn di chuyển ở thời đại đang xét không. */
    public boolean solidIn(TimeState state) {
        return solid && state.in(timeMask);
    }

    public boolean solidAnyTime() {
        return solid;
    }

    public boolean isHazard() {
        return hazard;
    }

    public int timeMask() {
        return timeMask;
    }

    /** Ánh xạ ký tự bản đồ -> loại ô. */
    public static TileType fromSymbol(char c) {
        for (TileType t : values()) {
            if (t.symbol == c) {
                return t;
            }
        }
        return EMPTY;
    }

    /** Màu vẽ của ô theo thời đại hiện tại. */
    public Color colorFor(TimeState state) {
        Color base = state.terrain();
        switch (this) {
            case GROUND:
                return base;
            case BRIDGE:
                return state == TimeState.PAST ? new Color(0x8A6B3A)
                        : new Color(0x6E4A9E);
            case PLATFORM_PAST:
                return new Color(0x8A6B3A);
            case PLATFORM_FUTURE:
                return new Color(0x7A3FA8);
            case ICE:
                return new Color(0x9FD8F0);
            case SPIKE:
                return new Color(0xC0392B);
            case LAVA:
                return new Color(0xE8622A);
            case DECOR:
                return new Color(base.getRed() / 2, base.getGreen() / 2, base.getBlue() / 2);
            default:
                return base;
        }
    }
}
