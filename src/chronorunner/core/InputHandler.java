package chronorunner.core;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Gom toàn bộ sự kiện bàn phím / chuột thành trạng thái mà game đọc được.
 *
 * <p>Có hai loại truy vấn:</p>
 * <ul>
 *   <li>{@code isDown(...)}  - phím đang được giữ (dùng cho di chuyển).</li>
 *   <li>{@code justPressed(...)} - phím vừa được nhấn trong bước cập nhật này
 *       (dùng cho nhảy, dash, tấn công...).</li>
 * </ul>
 *
 * <p>Vì vòng lặp game chạy theo bước cố định, {@code justPressed} chỉ được xoá
 * ở cuối mỗi bước cập nhật - nhờ vậy một lần nhấn không bị "nuốt" khi khung
 * hình bị trễ.</p>
 */
public class InputHandler extends KeyAdapter
        implements MouseListener, MouseMotionListener, FocusListener {

    // ---- Bảng phím mặc định ------------------------------------------------
    public static final int K_LEFT = KeyEvent.VK_A;
    public static final int K_RIGHT = KeyEvent.VK_D;
    public static final int K_LEFT_ALT = KeyEvent.VK_LEFT;
    public static final int K_RIGHT_ALT = KeyEvent.VK_RIGHT;
    public static final int K_JUMP = KeyEvent.VK_SPACE;
    public static final int K_DASH = KeyEvent.VK_SHIFT;
    public static final int K_ATTACK = KeyEvent.VK_J;
    public static final int K_REWIND = KeyEvent.VK_Q;
    public static final int K_INTERACT = KeyEvent.VK_E;
    public static final int K_FREEZE = KeyEvent.VK_F;
    public static final int K_PAUSE = KeyEvent.VK_ESCAPE;
    public static final int K_CONFIRM = KeyEvent.VK_ENTER;
    public static final int K_RESTART = KeyEvent.VK_R;

    private final Set<Integer> held = new HashSet<>();
    private final Set<Integer> pressedThisStep = new HashSet<>();

    // Toạ độ chuột thô (theo hệ của cửa sổ) và phép biến đổi sang hệ của
    // bộ đệm đồ hoạ 1280x720, do GamePanel thiết lập.
    private int rawMouseX;
    private int rawMouseY;
    private double mouseOffsetX;
    private double mouseOffsetY;
    private double mouseScaleX = 1;
    private double mouseScaleY = 1;

    private boolean mouseDown;
    private boolean mouseClickedThisStep;

    // ---- Bàn phím ----------------------------------------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (held.add(code)) {
            pressedThisStep.add(code);
        }
        // Chặn phím Space / mũi tên làm cuộn panel.
        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        held.remove(e.getKeyCode());
        e.consume();
    }

    // ---- Chuột -------------------------------------------------------------
    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        mouseClickedThisStep = true;
        rawMouseX = e.getX();
        rawMouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        rawMouseX = e.getX();
        rawMouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Đã xử lý ở mousePressed để phản hồi nhanh hơn.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // không cần xử lý
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseDown = false;
    }

    // ---- Mất focus: nhả toàn bộ phím để tránh "kẹt phím" --------------------
    @Override
    public void focusLost(FocusEvent e) {
        held.clear();
        mouseDown = false;
    }

    @Override
    public void focusGained(FocusEvent e) {
        // không cần xử lý
    }

    // ---- Truy vấn ----------------------------------------------------------
    public boolean isDown(int code) {
        return held.contains(code);
    }

    public boolean justPressed(int code) {
        return pressedThisStep.contains(code);
    }

    /** Bất kỳ phím nào vừa được nhấn (dùng cho màn hình "nhấn để tiếp tục"). */
    public boolean anyJustPressed() {
        return !pressedThisStep.isEmpty() || mouseClickedThisStep;
    }

    public boolean left() {
        return isDown(K_LEFT) || isDown(K_LEFT_ALT);
    }

    public boolean right() {
        return isDown(K_RIGHT) || isDown(K_RIGHT_ALT);
    }

    public boolean up() {
        return isDown(KeyEvent.VK_W) || isDown(KeyEvent.VK_UP);
    }

    public boolean down() {
        return isDown(KeyEvent.VK_S) || isDown(KeyEvent.VK_DOWN);
    }

    public boolean jumpHeld() {
        return isDown(K_JUMP) || isDown(KeyEvent.VK_W) || isDown(KeyEvent.VK_UP);
    }

    public boolean jumpPressed() {
        return justPressed(K_JUMP) || justPressed(KeyEvent.VK_W) || justPressed(KeyEvent.VK_UP);
    }

    public boolean dashPressed() {
        return justPressed(K_DASH);
    }

    public boolean attackPressed() {
        return justPressed(K_ATTACK);
    }

    public boolean rewindPressed() {
        return justPressed(K_REWIND);
    }

    public boolean interactPressed() {
        return justPressed(K_INTERACT);
    }

    public boolean freezePressed() {
        return justPressed(K_FREEZE);
    }

    public boolean pausePressed() {
        return justPressed(K_PAUSE);
    }

    public boolean confirmPressed() {
        return justPressed(K_CONFIRM);
    }

    public boolean restartPressed() {
        return justPressed(K_RESTART);
    }

    /**
     * Thiết lập phép biến đổi từ toạ độ cửa sổ sang toạ độ bộ đệm đồ hoạ.
     *
     * @param offsetX  lề trái của vùng vẽ (viền đen)
     * @param offsetY  lề trên của vùng vẽ
     * @param scaleX   tỉ lệ thu phóng theo trục X
     * @param scaleY   tỉ lệ thu phóng theo trục Y
     */
    public void setMouseTransform(double offsetX, double offsetY, double scaleX, double scaleY) {
        this.mouseOffsetX = offsetX;
        this.mouseOffsetY = offsetY;
        this.mouseScaleX = scaleX;
        this.mouseScaleY = scaleY;
    }

    public int mouseX() {
        return (int) Math.round((rawMouseX - mouseOffsetX) * mouseScaleX);
    }

    public int mouseY() {
        return (int) Math.round((rawMouseY - mouseOffsetY) * mouseScaleY);
    }

    public boolean mouseDown() {
        return mouseDown;
    }

    public boolean mouseJustPressed() {
        return mouseClickedThisStep;
    }

    /**
     * Xoá các phím "vừa nhấn". Gọi ở cuối mỗi bước cập nhật vật lý.
     */
    public void endStep() {
        pressedThisStep.clear();
        mouseClickedThisStep = false;
    }

    /** Dùng khi chuyển màn hình để không rò rỉ phím của màn trước. */
    public void flush() {
        pressedThisStep.clear();
        mouseClickedThisStep = false;
    }
}
