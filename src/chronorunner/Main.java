package chronorunner;

import chronorunner.core.Game;
import chronorunner.ui.GameWindow;
import chronorunner.ui.MainMenuScreen;

import javax.swing.SwingUtilities;

/**
 * Điểm khởi động của Chrono Runner.
 *
 * <p>Toàn bộ giao diện được tạo trong luồng sự kiện Swing (EDT); luồng mô phỏng
 * vật lý do {@code GamePanel} tự quản lý.</p>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            game.setScreen(new MainMenuScreen());

            GameWindow window = new GameWindow(game);
            window.launch();
        });
    }
}
