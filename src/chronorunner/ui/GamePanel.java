package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.InputHandler;
import chronorunner.core.Physics;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * Bề mặt vẽ và vòng lặp trò chơi.
 *
 * <p>Mô phỏng chạy theo <b>bước thời gian cố định</b> ({@link Physics#FIXED_DT})
 * trên một luồng riêng, còn việc vẽ được thực hiện vào một bộ đệm 1280x720 rồi
 * phóng to vừa khung cửa sổ (giữ đúng tỉ lệ, thêm viền đen nếu cần).</p>
 *
 * <p>Vì mô phỏng và vẽ nằm trên hai luồng khác nhau, cả hai đều được đồng bộ
 * trên chính đối tượng panel - nhờ đó trạng thái trò chơi không bao giờ bị
 * thay đổi giữa lúc đang vẽ.</p>
 */
public class GamePanel extends JPanel implements Runnable {

    /** Số bước mô phỏng tối đa cho một khung hình (chống "vòng xoáy chết"). */
    private static final int MAX_STEPS_PER_FRAME = 5;

    /** Khung hình dài hơn ngưỡng này (do cửa sổ bị treo) sẽ bị cắt bớt. */
    private static final double MAX_FRAME_SECONDS = 0.25;

    private static final long TARGET_FRAME_NANOS = 16_666_667L;

    private final Game game;
    private final BufferedImage buffer;

    private Thread thread;
    private volatile boolean running;
    private double accumulator;

    public GamePanel(Game game) {
        this.game = game;
        this.buffer = new BufferedImage(Game.VIEW_W, Game.VIEW_H, BufferedImage.TYPE_INT_RGB);

        setPreferredSize(new Dimension(Game.VIEW_W, Game.VIEW_H));
        setMinimumSize(new Dimension(640, 360));
        setBackground(new Color(0x05070C));
        setFocusable(true);
        setDoubleBuffered(true);

        InputHandler input = game.input();
        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);
        addFocusListener(input);
    }

    /** Khởi động luồng mô phỏng. */
    public void start() {
        if (thread != null) {
            return;
        }
        running = true;
        thread = new Thread(this, "chrono-runner-loop");
        thread.setDaemon(true);
        thread.start();
    }

    /** Dừng luồng mô phỏng (gọi khi đóng cửa sổ). */
    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void run() {
        long previous = System.nanoTime();

        while (running) {
            long frameStart = System.nanoTime();
            double frame = (frameStart - previous) / 1_000_000_000.0;
            previous = frameStart;
            if (frame > MAX_FRAME_SECONDS) {
                frame = MAX_FRAME_SECONDS;
            }
            accumulator += frame;

            int steps = 0;
            while (accumulator >= Physics.FIXED_DT && steps < MAX_STEPS_PER_FRAME) {
                synchronized (this) {
                    game.update(Physics.FIXED_DT);
                    game.input().endStep();
                }
                accumulator -= Physics.FIXED_DT;
                steps++;
            }
            if (steps == MAX_STEPS_PER_FRAME) {
                // Tụt hậu quá nhiều: bỏ phần thời gian dồn ứ để không trôi dần.
                accumulator = 0;
            }

            repaint();
            sleepRemaining(frameStart);
        }
    }

    private void sleepRemaining(long frameStart) {
        long elapsed = System.nanoTime() - frameStart;
        long remaining = TARGET_FRAME_NANOS - elapsed;
        if (remaining <= 0) {
            return;
        }
        try {
            Thread.sleep(remaining / 1_000_000L, (int) (remaining % 1_000_000L));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        synchronized (this) {
            Graphics2D bg = buffer.createGraphics();
            UiKit.prepare(bg);
            bg.setColor(UiKit.BACKDROP);
            bg.fillRect(0, 0, Game.VIEW_W, Game.VIEW_H);
            game.render(bg);
            bg.dispose();
        }

        double scale = Math.min(w / (double) Game.VIEW_W, h / (double) Game.VIEW_H);
        int dw = Math.max(1, (int) Math.round(Game.VIEW_W * scale));
        int dh = Math.max(1, (int) Math.round(Game.VIEW_H * scale));
        int dx = (w - dw) / 2;
        int dy = (h - dh) / 2;

        // Chuyển toạ độ chuột từ cửa sổ sang hệ 1280x720 của bộ đệm.
        game.input().setMouseTransform(dx, dy,
                Game.VIEW_W / (double) dw, Game.VIEW_H / (double) dh);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(buffer, dx, dy, dw, dh, null);
        g2.dispose();

        Toolkit.getDefaultToolkit().sync();
    }
}
