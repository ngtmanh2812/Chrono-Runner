package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.Screen;
import chronorunner.world.LevelFactory;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Lớp phủ tạm dừng.
 *
 * <p>Vì {@link #transparent()} trả về {@code true}, khung cảnh phía sau
 * vẫn được vẽ nhưng <b>không</b> được cập nhật - trò chơi đứng yên hoàn toàn
 * cho tới khi người chơi chọn tiếp tục.</p>
 */
public class PauseScreen implements Screen {

    private final int levelIndex;
    private final UiKit.Button[] buttons = new UiKit.Button[4];

    public PauseScreen(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    @Override
    public void onEnter(Game game) {
        double x = Game.VIEW_W / 2.0 - 190;
        double y = 262;
        double w = 380;
        double h = 56;

        buttons[0] = UiKit.button("TIẾP TỤC", x, y, w, h)
                .hint("Quay lại màn chơi")
                .action(() -> game.popScreen());
        buttons[1] = UiKit.button("CHƠI LẠI MÀN", x, y + 70, w, h)
                .hint("Bắt đầu lại từ đầu màn này")
                .action(() -> game.setScreen(
                        new PlayScreen(LevelFactory.create(game, levelIndex))));
        buttons[2] = UiKit.button("NÂNG CẤP", x, y + 140, w, h)
                .hint("Mua nâng cấp bằng Chrono Coin")
                .action(() -> game.pushScreen(new UpgradeScreen()));
        buttons[3] = UiKit.button("VỀ MENU CHÍNH", x, y + 210, w, h)
                .hint("Bỏ dở màn chơi hiện tại")
                .action(() -> {
                    game.save();
                    game.setScreen(new MainMenuScreen());
                });
    }

    @Override
    public void update(Game game, double dt) {
        for (UiKit.Button b : buttons) {
            UiKit.update(b, game.input());
        }
        if (game.input().pausePressed() || game.input().confirmPressed()) {
            game.popScreen();
        }
    }

    @Override
    public void render(Game game, Graphics2D g) {
        UiKit.scrim(g, Game.VIEW_W, Game.VIEW_H, 165);

        double pw = 520;
        double ph = 470;
        double px = (Game.VIEW_W - pw) / 2;
        double py = 168;

        UiKit.panel(g, px, py, pw, ph, UiKit.alpha(new Color(0x0F1626), 240), UiKit.EDGE, 18);

        UiKit.textCenter(g, "TẠM DỪNG", Game.VIEW_W / 2.0, py + 66, UiKit.bold(40), UiKit.TEXT);
        UiKit.textCenter(g, "Chrono Runner - Kẻ Du Hành Thời Gian",
                Game.VIEW_W / 2.0, py + 92, UiKit.regular(15), UiKit.TEXT_DIM);

        for (UiKit.Button b : buttons) {
            UiKit.draw(g, b, UiKit.ACCENT);
        }

        UiKit.textCenter(g, "Nhấn [ESC] hoặc [ENTER] để tiếp tục",
                Game.VIEW_W / 2.0, py + ph - 22, UiKit.regular(14), UiKit.TEXT_MUTED);
    }
}
