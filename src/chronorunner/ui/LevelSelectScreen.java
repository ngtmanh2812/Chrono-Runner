package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.Screen;
import chronorunner.world.LevelFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình chọn màn chơi.
 *
 * <p>Mỗi màn là một thẻ hiển thị tên, số Time Shard tốt nhất và trạng thái
 * (đã phá / sẵn sàng / còn khoá). Màn kế tiếp chỉ mở khi màn trước đã hoàn
 * thành, tạo nhịp tiến bộ rõ ràng cho người chơi.</p>
 */
public class LevelSelectScreen implements Screen {

    private static final double CARD_W = 222;
    private static final double CARD_H = 306;
    private static final double GAP = 22;
    private static final double CARD_Y = 218;

    private final List<UiKit.Button> backButtons = new ArrayList<>();
    private double time;
    private int hoveredCard = -1;

    @Override
    public void onEnter(Game game) {
        backButtons.add(UiKit.button("QUAY LẠI", 40, 620, 200, 52)
                .action(() -> game.setScreen(new MainMenuScreen())));
    }

    private double cardX(int i) {
        int n = LevelFactory.LEVEL_COUNT;
        double total = n * CARD_W + (n - 1) * GAP;
        return (Game.VIEW_W - total) / 2 + i * (CARD_W + GAP);
    }

    @Override
    public void update(Game game, double dt) {
        time += dt;
        for (UiKit.Button b : backButtons) {
            UiKit.update(b, game.input());
        }

        int mx = game.input().mouseX();
        int my = game.input().mouseY();
        hoveredCard = -1;
        for (int i = 0; i < LevelFactory.LEVEL_COUNT; i++) {
            if (mx >= cardX(i) && mx <= cardX(i) + CARD_W && my >= CARD_Y && my <= CARD_Y + CARD_H) {
                hoveredCard = i;
                break;
            }
        }

        if (hoveredCard >= 0 && game.progress().isLevelUnlocked(hoveredCard)
                && game.input().mouseJustPressed()) {
            game.setScreen(new StoryScreen(hoveredCard));
            return;
        }

        // Phím tắt: 1..5 chọn nhanh màn tương ứng.
        for (int i = 0; i < LevelFactory.LEVEL_COUNT; i++) {
            if (game.input().justPressed(java.awt.event.KeyEvent.VK_1 + i)
                    && game.progress().isLevelUnlocked(i)) {
                game.setScreen(new StoryScreen(i));
                return;
            }
        }

        if (game.input().pausePressed() || game.input().confirmPressed()) {
            game.setScreen(new MainMenuScreen());
        }
    }

    @Override
    public void render(Game game, Graphics2D g) {
        UiKit.verticalGradient(g, Game.VIEW_W, Game.VIEW_H, new Color(0x0A1120), UiKit.BACKDROP);
        UiKit.starfield(g, Game.VIEW_W, Game.VIEW_H, time, UiKit.ACCENT);
        UiKit.vignette(g, Game.VIEW_W, Game.VIEW_H);

        UiKit.textCenterShadow(g, "CHỌN MÀN CHƠI", Game.VIEW_W / 2.0, 112,
                UiKit.bold(40), UiKit.TEXT, 2);
        UiKit.textCenter(g, "Mỗi màn mở ra một mảnh ghép của dòng thời gian",
                Game.VIEW_W / 2.0, 146, UiKit.regular(15), UiKit.TEXT_DIM);

        for (int i = 0; i < LevelFactory.LEVEL_COUNT; i++) {
            drawCard(g, game, i);
        }

        UiKit.textCenter(g, "Nhấn [1]..[5] để vào nhanh  •  [ESC] quay lại",
                Game.VIEW_W / 2.0, 600, UiKit.regular(13), UiKit.TEXT_MUTED);

        for (UiKit.Button b : backButtons) {
            UiKit.draw(g, b, UiKit.ACCENT);
        }
    }

    private void drawCard(Graphics2D g, Game game, int index) {
        boolean unlocked = game.progress().isLevelUnlocked(index);
        boolean completed = game.progress().isLevelCompleted(index);
        boolean hot = hoveredCard == index && unlocked;

        Color accent = completed ? UiKit.OK : (unlocked ? UiKit.ACCENT : UiKit.TEXT_MUTED);
        double x = cardX(index);
        double lift = hot ? -8 : 0;
        double y = CARD_Y + lift;

        UiKit.panel(g, x, y, CARD_W, CARD_H,
                UiKit.alpha(unlocked ? UiKit.PANEL : new Color(0x0D1118), 236),
                UiKit.alpha(accent, hot ? 255 : 150), 18);

        // Số thứ tự màn
        UiKit.textCenter(g, "MÀN " + (index + 1), x + CARD_W / 2, y + 36,
                UiKit.bold(14), UiKit.alpha(accent, 220));

        // Hoạ tiết đồng hồ
        UiKit.clockRing(g, x + CARD_W / 2, y + 108, 46,
                time * (0.6 + index * 0.12), UiKit.alpha(accent, unlocked ? 200 : 90), 2.6f);

        // Tên màn
        String name = LevelFactory.levelName(index);
        g.setFont(UiKit.bold(19));
        List<String> wrapped = UiKit.wrap(g, name, UiKit.bold(19), (int) CARD_W - 36);
        double ny = y + 190;
        for (String line : wrapped) {
            UiKit.textCenter(g, line, x + CARD_W / 2, ny, UiKit.bold(19),
                    unlocked ? UiKit.TEXT : UiKit.TEXT_MUTED);
            ny += 24;
        }

        // Thông tin shard
        int best = game.progress().bestShards(index);
        UiKit.shardIcon(g, x + 46, y + 246, 9, unlocked ? UiKit.SHARD : UiKit.TEXT_MUTED);
        UiKit.text(g, "Shard tốt nhất: " + best, x + 62, y + 251,
                UiKit.regular(13), UiKit.TEXT_DIM);

        // Trạng thái
        String status = !unlocked ? "CÒN KHOÁ" : (completed ? "ĐÃ PHÁ" : "SẴN SÀNG");
        Color statusColor = !unlocked ? UiKit.TEXT_MUTED : (completed ? UiKit.OK : UiKit.GOLD);
        UiKit.panel(g, x + 34, y + 266, CARD_W - 68, 28,
                UiKit.alpha(statusColor, 34), UiKit.alpha(statusColor, 160), 10);
        UiKit.textCenter(g, status, x + CARD_W / 2, y + 285, UiKit.bold(15), statusColor);

        if (!unlocked) {
            drawLock(g, x + CARD_W / 2, y + 108, 17);
        } else if (hot) {
            g.setColor(UiKit.alpha(accent, 40));
            g.fill(UiKit.roundRect(x + 3, y + 3, CARD_W - 6, CARD_H - 6, 16));
        }
    }

    private void drawLock(Graphics2D g, double cx, double cy, double r) {
        g.setColor(UiKit.alpha(UiKit.TEXT_MUTED, 190));
        g.setStroke(new BasicStroke(4f));
        g.drawArc((int) (cx - r * 0.62), (int) (cy - r), (int) (r * 1.24), (int) (r * 1.3), 0, 180);
        g.fillRoundRect((int) (cx - r * 0.85), (int) cy, (int) (r * 1.7), (int) (r * 1.35), 6, 6);
        g.setColor(UiKit.alpha(new Color(0x0D1118), 220));
        g.fillOval((int) (cx - 3), (int) (cy + 10), 6, 6);
    }
}
