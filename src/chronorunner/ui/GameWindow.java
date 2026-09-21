package chronorunner.ui;

import chronorunner.core.Game;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Cửa sổ chính của trò chơi.
 *
 * <p>Cửa sổ chỉ chịu trách nhiệm: đặt tiêu đề, chứa {@link GamePanel},
 * chuyển tiếp sự kiện focus và lưu tiến trình khi người chơi đóng cửa sổ.</p>
 */
public class GameWindow extends JFrame {

    private final GamePanel panel;

    public GameWindow(Game game) {
        super("Chrono Runner - Kẻ Du Hành Thời Gian");
        this.panel = new GamePanel(game);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                game.save();
                panel.stop();
                dispose();
                System.exit(0);
            }
        });

        // Bấm chuột vào cửa sổ sẽ giành lại focus bàn phím (tránh "kẹt phím").
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow();
            }
        });

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        setMinimumSize(new Dimension(960, 540));
        pack();
        setLocationRelativeTo(null);
        setResizable(true);
        setFocusTraversalKeysEnabled(false);
    }

    /** Hiện cửa sổ và bắt đầu vòng lặp trò chơi. */
    public void launch() {
        setVisible(true);
        panel.requestFocusInWindow();
        panel.start();
    }

    public GamePanel panel() {
        return panel;
    }
}
