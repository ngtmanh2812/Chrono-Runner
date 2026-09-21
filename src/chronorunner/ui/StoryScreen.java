package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.Screen;
import chronorunner.world.Level;
import chronorunner.world.LevelFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Màn hình dẫn truyện trước khi vào màn chơi.
 *
 * <p>Màn chơi được dựng sẵn ở đây rồi chuyển thẳng cho {@link PlayScreen},
 * nhờ vậy bản đồ chỉ được sinh ra một lần cho mỗi lượt chơi.</p>
 */
public class StoryScreen implements Screen {

    private final int index;

    private Level level;
    private List<String> lines;
    private UiKit.Button startButton;
    private UiKit.Button backButton;
    private double time;

    public StoryScreen(int index) {
        this.index = index;
    }

    @Override
    public void onEnter(Game game) {
        level = LevelFactory.create(game, index);

        double w = 300;
        double h = 54;
        startButton = UiKit.button("BẮT ĐẦU", Game.VIEW_W / 2.0 - w - 14, 596, w, h)
                .hint("Nhấn [ENTER] để vào màn")
                .action(() -> game.setScreen(new PlayScreen(level)));
        backButton = UiKit.button("QUAY LẠI", Game.VIEW_W / 2.0 + 14, 596, w, h)
                .action(() -> game.setScreen(new LevelSelectScreen()));
    }

    @Override
    public void update(Game game, double dt) {
        time += dt;
        UiKit.update(startButton, game.input());
        UiKit.update(backButton, game.input());
        if (game.input().confirmPressed()) {
            startButton.trigger();
        } else if (game.input().pausePressed()) {
            backButton.trigger();
        }
    }

    @Override
    public void render(Game game, Graphics2D g) {
        UiKit.verticalGradient(g, Game.VIEW_W, Game.VIEW_H, new Color(0x0A1220), UiKit.BACKDROP);
        UiKit.starfield(g, Game.VIEW_W, Game.VIEW_H, time, UiKit.ACCENT);
        UiKit.vignette(g, Game.VIEW_W, Game.VIEW_H);

        double cx = Game.VIEW_W / 2.0;
        UiKit.clockRing(g, cx, 168, 74, time * 0.9, UiKit.alpha(UiKit.ACCENT, 200), 3.2f);

        UiKit.textCenter(g, level.subtitle().toUpperCase(), cx, 284,
                UiKit.bold(17), UiKit.ACCENT);
        UiKit.textCenterShadow(g, level.name().toUpperCase(), cx, 340,
                UiKit.bold(46), UiKit.TEXT, 2);

        // Khung nội dung dẫn truyện
        double pw = 760;
        double ph = 176;
        double px = cx - pw / 2;
        double py = 384;
        UiKit.panel(g, px, py, pw, ph, UiKit.alpha(new Color(0x101827), 225), UiKit.EDGE, 16);

        if (lines == null) {
            lines = UiKit.wrap(g, level.intro(), UiKit.regular(16), (int) pw - 64);
        }
        double ly = py + 40;
        for (String line : lines) {
            UiKit.text(g, line, px + 32, ly, UiKit.regular(16), UiKit.TEXT);
            ly += 23;
        }

        UiKit.textCenter(g, "Time Shard trong màn: xem HUD  •  Cần tối thiểu "
                        + level.requiredShards() + " shard để mở cổng ra",
                cx, 580, UiKit.regular(13), UiKit.TEXT_MUTED);

        UiKit.draw(g, startButton, UiKit.OK);
        UiKit.draw(g, backButton, UiKit.ACCENT);
    }
}
