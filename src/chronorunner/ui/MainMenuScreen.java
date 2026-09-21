package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.Screen;
import chronorunner.core.TimeState;
import chronorunner.util.SaveManager;
import chronorunner.world.LevelFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu chính của trò chơi.
 *
 * <p>Nền menu đổi màu theo chu kỳ ba thời đại để gợi nhắc cơ chế cốt lõi
 * ngay từ màn hình đầu tiên.</p>
 */
public class MainMenuScreen implements Screen {

    private final List<UiKit.Button> buttons = new ArrayList<>();
    private UiKit.Button confirmYes;
    private UiKit.Button confirmNo;

    private double time;
    private boolean confirmReset;
    private String notice;
    private double noticeTimer;

    @Override
    public void onEnter(Game game) {
        double cx = Game.VIEW_W / 2.0;
        double w = 360;
        double h = 56;
        double x = cx - w / 2;
        double y = 300;
        double step = 70;

        buttons.add(UiKit.button("BẮT ĐẦU HÀNH TRÌNH", x, y, w, h)
                .hint("Chọn màn chơi và bước vào dòng thời gian")
                .action(() -> game.setScreen(new LevelSelectScreen())));
        y += step;

        int next = firstUnlockedLevel(game);
        buttons.add(UiKit.button("CHƠI TIẾP", x, y, w, h)
                .hint("Vào ngay " + LevelFactory.levelName(next))
                .action(() -> game.setScreen(new StoryScreen(next))));
        y += step;

        buttons.add(UiKit.button("NÂNG CẤP CHRONO CORE", x, y, w, h)
                .hint("Tua ngược, Dash, Nhảy đôi, Đóng băng")
                .action(() -> game.pushScreen(new UpgradeScreen())));
        y += step;

        buttons.add(UiKit.button("XOÁ TIẾN TRÌNH", x, y, w, h)
                .hint("Xoá toàn bộ tệp lưu và bắt đầu lại")
                .action(() -> confirmReset = true));
        y += step;

        buttons.add(UiKit.button("THOÁT", x, y, w, h)
                .action(() -> System.exit(0)));

        double mw = 260;
        confirmYes = UiKit.button("XOÁ HẾT", cx - mw - 12, 400, mw, 54)
                .action(() -> {
                    SaveManager.deleteSave();
                    game.progress().reset();
                    confirmReset = false;
                    notice = "Đã xoá tiến trình. Chúc may mắn ở lần chạy tiếp theo!";
                    noticeTimer = 3.6;
                });
        confirmNo = UiKit.button("HUỶ", cx + 12, 400, mw, 54)
                .action(() -> confirmReset = false);
    }

    /** Màn xa nhất mà người chơi đã mở khoá. */
    private int firstUnlockedLevel(Game game) {
        int best = 0;
        for (int i = 0; i < LevelFactory.LEVEL_COUNT; i++) {
            if (game.progress().isLevelUnlocked(i)) {
                best = i;
            }
        }
        return best;
    }

    @Override
    public void update(Game game, double dt) {
        time += dt;

        if (confirmReset) {
            UiKit.update(confirmYes, game.input());
            UiKit.update(confirmNo, game.input());
            if (game.input().pausePressed()) {
                confirmReset = false;
            }
            return;
        }

        if (notice != null) {
            // Thông báo tự tắt sau vài giây.
            noticeTimer -= dt;
            if (noticeTimer <= 0) {
                notice = null;
            }
        }
        for (UiKit.Button b : buttons) {
            UiKit.update(b, game.input());
        }
        if (game.input().confirmPressed()) {
            buttons.get(0).trigger();
        }
    }

    @Override
    public void render(Game game, Graphics2D g) {
        renderBackdrop(g);

        double cx = Game.VIEW_W / 2.0;

        UiKit.clockRing(g, cx, 132, 104, time * 0.55, UiKit.alpha(state().accent(), 150), 3f);

        UiKit.textCenterShadow(g, "CHRONO RUNNER", cx, 118, UiKit.bold(66), UiKit.TEXT, 2);
        UiKit.textCenter(g, "KẺ DU HÀNH THỜI GIAN", cx, 160, UiKit.bold(22),
                UiKit.alpha(state().accent(), 235));
        UiKit.textCenter(g, "Platformer  •  Giải đố thời gian  •  5 màn chơi",
                cx, 192, UiKit.regular(15), UiKit.TEXT_DIM);

        if (confirmReset) {
            renderConfirm(g, cx);
        } else {
            for (UiKit.Button b : buttons) {
                UiKit.draw(g, b, state().accent());
            }
        }

        renderStatusBar(g, game);
    }

    private void renderConfirm(Graphics2D g, double cx) {
        double pw = 600;
        double ph = 240;
        double px = cx - pw / 2;
        double py = 320;
        UiKit.panel(g, px, py, pw, ph, UiKit.alpha(new Color(0x1A0A10), 240),
                UiKit.alpha(UiKit.DANGER, 200), 18);
        UiKit.textCenter(g, "XOÁ TOÀN BỘ TIẾN TRÌNH?", cx, py + 56,
                UiKit.bold(26), UiKit.TEXT);
        UiKit.textCenter(g, "Coin, nâng cấp và các màn đã mở sẽ mất vĩnh viễn.",
                cx, py + 92, UiKit.regular(15), UiKit.TEXT_DIM);
        UiKit.textCenter(g, "Hành động này không thể hoàn tác.",
                cx, py + 116, UiKit.regular(14), UiKit.DANGER);

        UiKit.draw(g, confirmYes, UiKit.DANGER);
        UiKit.draw(g, confirmNo, UiKit.ACCENT);
    }

    private void renderStatusBar(Graphics2D g, Game game) {
        double y = Game.VIEW_H - 34;

        UiKit.text(g, "[A/D] di chuyển   [SPACE] nhảy   [SHIFT] dash   [J] chém",
                28, y, UiKit.regular(13), UiKit.TEXT_MUTED);
        UiKit.text(g, "[E] đổi thời đại   [Q] tua ngược   [F] đóng băng   [ESC] tạm dừng",
                28, y + 20, UiKit.regular(13), UiKit.TEXT_MUTED);

        UiKit.textRight(g, "Chrono Coin: " + game.progress().coins(),
                Game.VIEW_W - 28, y, UiKit.bold(14), UiKit.GOLD);
        UiKit.textRight(g, "Màn đã phá: " + game.progress().completedCount()
                        + " / " + LevelFactory.LEVEL_COUNT,
                Game.VIEW_W - 28, y + 20, UiKit.regular(13), UiKit.TEXT_DIM);

        if (notice != null) {
            UiKit.textCenter(g, notice, Game.VIEW_W / 2.0, Game.VIEW_H - 64,
                    UiKit.bold(15), UiKit.OK);
        }
    }

    /** Thời đại đang dùng để tô màu menu (đổi dần theo thời gian). */
    private TimeState state() {
        TimeState[] all = TimeState.values();
        return all[(int) (time * 0.22) % all.length];
    }

    private void renderBackdrop(Graphics2D g) {
        TimeState s = state();
        UiKit.verticalGradient(g, Game.VIEW_W, Game.VIEW_H, s.skyTop(), UiKit.BACKDROP);

        // Đường chân trời cách điệu
        double parallax = time * 12;
        g.setColor(UiKit.alpha(s.skyBottom(), 210));
        for (int i = -1; i < 16; i++) {
            double bx = i * 150 - (parallax % 150);
            int bw = 80 + (i * 41 % 60);
            int bh = 70 + (Math.abs(i * 67) % 190);
            g.fillRect((int) bx, Game.VIEW_H - 120 - bh, bw, bh + 120);
        }

        UiKit.starfield(g, Game.VIEW_W, Game.VIEW_H, time, s.accent());
        UiKit.vignette(g, Game.VIEW_W, Game.VIEW_H);
    }
}
