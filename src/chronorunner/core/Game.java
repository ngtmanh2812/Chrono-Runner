package chronorunner.core;

import chronorunner.util.Progress;
import chronorunner.util.SaveManager;
import chronorunner.world.Level;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bộ điều phối trung tâm của trò chơi.
 *
 * <p>Game nắm giữ những thứ tồn tại xuyên suốt mọi màn hình:</p>
 * <ul>
 *   <li>{@link InputHandler} - bàn phím và chuột,</li>
 *   <li>{@link Camera} - ống kính của màn chơi,</li>
 *   <li>{@link Progress} - tiến trình được lưu xuống đĩa,</li>
 *   <li><b>ngăn xếp màn hình</b> (screen stack) - cho phép màn tạm dừng
 *       phủ lên màn chơi mà không mất trạng thái bên dưới.</li>
 * </ul>
 *
 * <p>Mọi màn hình đều giao tiếp với phần còn lại của trò chơi qua lớp này,
 * nhờ đó các lớp UI không cần biết đến nhau.</p>
 */
public class Game {

    /** Độ phân giải nội bộ cố định của trò chơi. */
    public static final int VIEW_W = 1280;
    public static final int VIEW_H = 720;

    /** Số mạng của người chơi trong một màn. */
    public static final int LIVES_PER_LEVEL = 3;

    /** Callback để màn chơi báo ngược lại cho tầng điều phối. */
    public interface LevelListener {
        /** Người chơi chạm cổng ra đang mở. */
        void onLevelComplete();

        /** Người chơi mất hết máu. */
        void onPlayerDied();
    }

    /** Một dòng thông báo nổi ở góc màn hình. */
    public static final class Toast {
        private final String text;
        private final double maxLife;
        private double life;

        private Toast(String text, double life) {
            this.text = text;
            this.maxLife = life;
            this.life = life;
        }

        public String text() {
            return text;
        }

        /** Độ mờ 0..1, tự giảm dần khi thông báo sắp tắt. */
        public float alpha() {
            double t = life / Math.max(0.001, maxLife);
            return (float) Math.max(0, Math.min(1, t * 2.2));
        }
    }

    private static final double TOAST_LIFE = 2.6;
    private static final int MAX_TOASTS = 5;

    private final InputHandler input = new InputHandler();
    private final Camera camera = new Camera(VIEW_W, VIEW_H);
    private final Progress progress;

    private final List<Screen> stack = new ArrayList<>();
    private final List<Toast> toasts = new ArrayList<>();
    private final List<Toast> toastsView = Collections.unmodifiableList(toasts);

    private Level level;
    private LevelListener levelListener;
    private String prompt = "";
    private double totalTime;

    public Game() {
        this(SaveManager.load());
    }

    public Game(Progress progress) {
        this.progress = progress;
    }

    // ---- Truy cập nền tảng -------------------------------------------------
    public InputHandler input() {
        return input;
    }

    public Camera camera() {
        return camera;
    }

    public Progress progress() {
        return progress;
    }

    /** Màn chơi đang được nạp, hoặc {@code null} nếu đang ở menu. */
    public Level level() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setLevelListener(LevelListener listener) {
        this.levelListener = listener;
    }

    /** Tổng thời gian đã chơi trong phiên hiện tại (giây). */
    public double totalTime() {
        return totalTime;
    }

    public void save() {
        SaveManager.save(progress);
    }

    // ---- Thông báo & gợi ý -------------------------------------------------
    /** Đẩy một thông báo nổi. Thông báo trùng nội dung sẽ được "làm mới". */
    public void toast(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        for (Toast t : toasts) {
            if (t.text.equals(text)) {
                t.life = t.maxLife;
                return;
            }
        }
        toasts.add(new Toast(text, TOAST_LIFE));
        while (toasts.size() > MAX_TOASTS) {
            toasts.remove(0);
        }
    }

    public List<Toast> toasts() {
        return toastsView;
    }

    /**
     * Gợi ý ngữ cảnh, được xoá lại ở đầu mỗi bước cập nhật.
     * Vật thể trong màn gọi phương thức này khi người chơi đứng gần.
     */
    public void requestPrompt(String text) {
        this.prompt = text == null ? "" : text;
    }

    public String prompt() {
        return prompt;
    }

    // ---- Tiến trình --------------------------------------------------------
    public void addCoins(int amount) {
        if (amount != 0) {
            progress.addCoins(amount);
        }
    }

    public void onLevelComplete() {
        if (levelListener != null) {
            levelListener.onLevelComplete();
        }
    }

    public void onPlayerDied() {
        if (levelListener != null) {
            levelListener.onPlayerDied();
        }
    }

    // ---- Ngăn xếp màn hình -------------------------------------------------
    /** Thay thế toàn bộ ngăn xếp bằng một màn hình mới. */
    public void setScreen(Screen screen) {
        while (!stack.isEmpty()) {
            stack.remove(stack.size() - 1).onExit(this);
        }
        level = null;
        pushScreen(screen);
    }

    /** Đẩy một màn hình lên trên (ví dụ: tạm dừng, kết quả màn). */
    public void pushScreen(Screen screen) {
        stack.add(screen);
        input.flush();
        screen.onEnter(this);
    }

    /** Đóng màn hình trên cùng và quay lại màn hình bên dưới. */
    public void popScreen() {
        if (stack.isEmpty()) {
            return;
        }
        stack.remove(stack.size() - 1).onExit(this);
        input.flush();
        if (!stack.isEmpty()) {
            stack.get(stack.size() - 1).onResume(this);
        }
    }

    public Screen currentScreen() {
        return stack.isEmpty() ? null : stack.get(stack.size() - 1);
    }

    public int screenDepth() {
        return stack.size();
    }

    // ---- Vòng lặp ----------------------------------------------------------
    /** Một bước cập nhật logic. {@code dt} luôn là bước cố định. */
    public void update(double dt) {
        totalTime += dt;
        prompt = "";

        for (int i = toasts.size() - 1; i >= 0; i--) {
            Toast t = toasts.get(i);
            t.life -= dt;
            if (t.life <= 0) {
                toasts.remove(i);
            }
        }

        Screen top = currentScreen();
        if (top != null) {
            top.update(this, dt);
        }
    }

    /**
     * Vẽ toàn bộ ngăn xếp màn hình, bắt đầu từ màn hình đục (opaque)
     * trên cùng - nhờ vậy màn tạm dừng vẫn thấy được màn chơi phía sau.
     */
    public void render(Graphics2D g) {
        if (stack.isEmpty()) {
            return;
        }
        int start = 0;
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (!stack.get(i).transparent()) {
                start = i;
                break;
            }
        }
        for (int i = start; i < stack.size(); i++) {
            stack.get(i).render(this, g);
        }
    }
}
