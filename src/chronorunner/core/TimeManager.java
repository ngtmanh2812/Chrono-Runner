package chronorunner.core;

import chronorunner.entity.GameObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý trục thời gian của màn chơi.
 *
 * <p>Chịu trách nhiệm:</p>
 * <ul>
 *   <li>giữ thời đại hiện tại (QUÁ KHỨ / HIỆN TẠI / TƯƠNG LAI),</li>
 *   <li>thông báo cho mọi vật thể đã đăng ký khi thời đại đổi,</li>
 *   <li>quản lý hiệu ứng Đóng Băng Thời Gian.</li>
 * </ul>
 *
 * <p>Nhờ cơ chế đăng ký - thông báo (observer), vật thể không cần tự hỏi
 * "thời đại đã đổi chưa" mỗi khung hình.</p>
 */
public class TimeManager {

    private TimeState current = TimeState.PRESENT;
    private double freezeTimer;

    private final List<GameObject> listeners = new ArrayList<>();

    public TimeState current() {
        return current;
    }

    /** Đăng ký một vật thể để nhận thông báo khi thời đại thay đổi. */
    public void register(GameObject object) {
        if (!listeners.contains(object)) {
            listeners.add(object);
        }
    }

    public void unregister(GameObject object) {
        listeners.remove(object);
    }

    public void clearListeners() {
        listeners.clear();
    }

    /** Dọn những vật thể đã bị tiêu huỷ khỏi danh sách theo dõi. */
    public void unregisterIfDead() {
        listeners.removeIf(o -> !o.isAlive());
    }

    /**
     * Chuyển sang thời đại mới và thông báo cho các vật thể.
     *
     * @return {@code true} nếu thời đại thực sự thay đổi
     */
    public boolean shiftTo(TimeState next) {
        if (next == null || next == current) {
            return false;
        }
        TimeState previous = current;
        current = next;
        for (GameObject o : listeners) {
            o.onTimeChanged(previous, next);
        }
        return true;
    }

    public void freeze(double seconds) {
        freezeTimer = Math.max(freezeTimer, seconds);
    }

    public boolean isFrozen() {
        return freezeTimer > 0;
    }

    public void clearFreeze() {
        freezeTimer = 0;
    }

    public double freezeRemaining() {
        return Math.max(0, freezeTimer);
    }

    public void update(double dt) {
        if (freezeTimer > 0) {
            freezeTimer -= dt;
            if (freezeTimer < 0) {
                freezeTimer = 0;
            }
        }
    }
}
