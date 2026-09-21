package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.Screen;
import chronorunner.world.Level;
import chronorunner.world.LevelFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình tổng kết sau mỗi màn chơi (thành công hoặc thất bại).
 *
 * <p>Đây là nơi duy nhất ghi tiến trình hoàn thành màn xuống tệp lưu,
 * nhờ đó việc "hoàn thành" chỉ được tính đúng một lần cho mỗi lượt chơi.</p>
 */
public class ResultScreen implements Screen {

    private static final int BONUS_BASE = 40;
    private static final int BONUS_PER_SHARD = 8;
    private static final int FINAL_BONUS = 300;

    private static final double PANEL_X = 360;
    private static final double PANEL_Y = 50;
    private static final double PANEL_W = 560;
    private static final double PANEL_H = 620;

    private final Level level;
    private final boolean success;
    private final List<UiKit.Button> buttons = new ArrayList<>();

    private int bonusCoins;
    private boolean finalVictory;
    private double time;

    public ResultScreen(Level level, boolean success) {
        this.level = level;
        this.success = success;
    }

    @Override
    public void onEnter(Game game) {
        finalVictory = success && level.index() >= LevelFactory.LEVEL_COUNT - 1;

        if (success) {
            bonusCoins = BONUS_BASE + level.shardsCollected() * BONUS_PER_SHARD;
            if (finalVictory) {
                bonusCoins += FINAL_BONUS;
            }
            game.addCoins(bonusCoins);
            game.progress().markCompleted(level.index(),
                    level.shardsCollected(), level.totalShards());
            game.save();
        }
        time = level.elapsed();

        int count = success ? 4 : 3;
        double w = 400;
        double h = 52;
        double step = 58;
        double x = Game.VIEW_W / 2.0 - w / 2;
        double y = PANEL_Y + PANEL_H - 26 - ((count - 1) * step + h);

        if (success && !finalVictory) {
            buttons.add(UiKit.button("MÀN TIẾP THEO", x, y, w, h)
                    .hint("Tiến sâu hơn vào dòng thời gian")
                    .action(() -> game.setScreen(new StoryScreen(level.index() + 1))));
            y += step;
        }
        if (finalVictory) {
            buttons.add(UiKit.button("PHÁ VỠ VÒNG LẶP", x, y, w, h)
                    .hint("Trở về menu chính")
                    .action(() -> game.setScreen(new MainMenuScreen())));
            y += step;
        }

        buttons.add(UiKit.button("CHƠI LẠI", x, y, w, h)
                .action(() -> game.setScreen(
                        new PlayScreen(LevelFactory.create(game, level.index())))));
        y += step;

        buttons.add(UiKit.button("NÂNG CẤP", x, y, w, h)
                .hint("Dùng Chrono Coin vừa kiếm được")
                .action(() -> game.pushScreen(new UpgradeScreen())));
        y += step;

        buttons.add(UiKit.button("CHỌN MÀN", x, y, w, h)
                .hint("Quay lại danh sách màn chơi")
                .action(() -> {
                    game.save();
                    game.setScreen(new LevelSelectScreen());
                }));
    }

    @Override
    public void update(Game game, double dt) {
        for (UiKit.Button b : buttons) {
            UiKit.update(b, game.input());
        }
        if (game.input().confirmPressed() && !buttons.isEmpty()) {
            buttons.get(0).trigger();
        }
    }

    @Override
    public void render(Game game, Graphics2D g) {
        renderBackdrop(game, g);

        Color tint = success ? UiKit.ACCENT : UiKit.DANGER;
        double cx = Game.VIEW_W / 2.0;
        double py = PANEL_Y;
        double ph = PANEL_H;

        UiKit.panel(g, PANEL_X, py, PANEL_W, ph, UiKit.alpha(new Color(0x0F1626), 238),
                UiKit.alpha(tint, 170), 20);

        UiKit.clockRing(g, cx, py + 92, 58, game.totalTime() * 0.8, UiKit.alpha(tint, 190), 3f);

        String title = success
                ? (finalVictory ? "VÒNG LẶP BỊ PHÁ VỠ" : "MÀN CHƠI HOÀN THÀNH")
                : "THẤT BẠI";
        UiKit.textCenterShadow(g, title, cx, py + 112, UiKit.bold(33),
                success ? UiKit.TEXT : UiKit.DANGER, 2);

        String sub = success
                ? level.subtitle() + " - " + level.name()
                : "Alex gục ngã trước khi kịp chạm cổng thời gian.";
        UiKit.textCenter(g, sub, cx, py + 142, UiKit.regular(15), UiKit.TEXT_DIM);

        double sx = PANEL_X + 52;
        double sy = py + 186;
        statRow(g, sx, sy, "Time Shard", level.shardsCollected() + " / " + level.totalShards(), UiKit.SHARD);
        statRow(g, sx, sy + 36, "Thời gian", UiKit.formatTime(time), UiKit.ACCENT);
        statRow(g, sx, sy + 72, "Chrono Coin nhặt được",
                String.valueOf(level.coinsCollected()), UiKit.GOLD);
        if (success) {
            statRow(g, sx, sy + 108, "Thưởng hoàn thành", "+" + bonusCoins, UiKit.OK);
        } else {
            statRow(g, sx, sy + 108, "Gợi ý", "Thu thập thêm Time Shard", UiKit.TEXT_DIM);
        }

        UiKit.text(g, "Tổng coin hiện có: " + game.progress().coins(),
                sx, sy + 148, UiKit.bold(15), UiKit.GOLD);

        if (finalVictory) {
            UiKit.textCenter(g, "Alex trở về nhà. Thành phố vẫn đứng vững.",
                    cx, sy + 180, UiKit.regular(15), UiKit.OK);
        }

        for (UiKit.Button b : buttons) {
            UiKit.draw(g, b, tint);
        }
    }

    /** Nền tối có sao trôi, thay đổi tông theo kết quả. */
    private void renderBackdrop(Game game, Graphics2D g) {
        UiKit.verticalGradient(g, Game.VIEW_W, Game.VIEW_H,
                success ? new Color(0x0A1622) : new Color(0x1A0A10),
                UiKit.BACKDROP);
        UiKit.starfield(g, Game.VIEW_W, Game.VIEW_H, game.totalTime(),
                success ? UiKit.ACCENT : UiKit.DANGER);
        UiKit.vignette(g, Game.VIEW_W, Game.VIEW_H);
    }

    private void statRow(Graphics2D g, double x, double y,
                         String label, String value, Color valueColor) {
        UiKit.text(g, label, x, y, UiKit.regular(15), UiKit.TEXT_DIM);
        UiKit.textRight(g, value, PANEL_X + PANEL_W - 52, y, UiKit.bold(16), valueColor);
    }
}
