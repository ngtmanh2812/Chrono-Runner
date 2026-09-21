package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.TimeState;
import chronorunner.entity.Player;
import chronorunner.entity.enemy.Enemy;
import chronorunner.world.Level;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Lớp phủ thông tin trong màn chơi (HUD).
 *
 * <p>HUD chỉ <b>đọc</b> trạng thái từ {@link Level} và {@link Player};
 * nó không bao giờ thay đổi logic trò chơi. Nhờ vậy có thể tắt HUD
 * (chế độ chụp ảnh) mà không ảnh hưởng gì tới mô phỏng.</p>
 */
public class Hud {

    private static final int PAD = 16;
    private double pulse;
    private double bannerTimer;
    private String banner = "";
    private Color bannerColor = Color.WHITE;
    private int lives = Game.LIVES_PER_LEVEL;

    /** Số mạng còn lại của màn chơi, do {@link PlayScreen} cập nhật. */
    public void setLives(int lives) {
        this.lives = Math.max(0, lives);
    }

    /** Hiện một dòng chữ lớn giữa màn hình trong vài giây. */
    public void banner(String text, Color color, double seconds) {
        this.banner = text;
        this.bannerColor = color;
        this.bannerTimer = seconds;
    }

    public void update(double dt) {
        pulse += dt;
        if (bannerTimer > 0) {
            bannerTimer -= dt;
        }
    }

    public void render(Graphics2D g, Game game, Level level) {
        Player p = level.player();

        renderVitals(g, p);
        renderCounters(g, game, level);
        renderCenter(g, level, p);
        renderAbilities(g, game, p);
        renderToasts(g, game);
        renderBoss(g, level);
        renderPrompt(g, game);
        renderBanner(g);
    }

    // ---- Góc trên trái: máu + năng lượng -----------------------------------
    private void renderVitals(Graphics2D g, Player p) {
        UiKit.panel(g, PAD, PAD, 312, 92, UiKit.alpha(UiKit.PANEL, 214), UiKit.EDGE, 12);

        UiKit.text(g, "SINH LỰC", PAD + 16, PAD + 26, UiKit.bold(13), UiKit.TEXT_DIM);
        if (p != null) {
            int hearts = p.maxHealth();
            for (int i = 0; i < hearts; i++) {
                UiKit.heartIcon(g, PAD + 26 + i * 26, PAD + 44, 11, i < p.health());
            }
        }

        double ratio = p == null ? 0 : p.energyRatio();
        Color energy = ratio < 0.25 ? UiKit.DANGER : UiKit.ACCENT;
        UiKit.bar(g, PAD + 16, PAD + 62, 280, 13, ratio, energy,
                new Color(0x0D1420), UiKit.alpha(energy, 150));
        String label = p == null ? "0 / 0"
                : (int) p.timeEnergy() + " / " + (int) p.maxTimeEnergy();
        UiKit.textRight(g, label, PAD + 296, PAD + 88, UiKit.regular(12), UiKit.TEXT_DIM);
        UiKit.text(g, "NĂNG LƯỢNG THỜI GIAN", PAD + 16, PAD + 88, UiKit.regular(12), UiKit.TEXT_MUTED);
    }

    // ---- Góc trên phải: shard + coin + mạng --------------------------------
    private void renderCounters(Graphics2D g, Game game, Level level) {
        int w = 300;
        int x = Game.VIEW_W - PAD - w;
        UiKit.panel(g, x, PAD, w, 92, UiKit.alpha(UiKit.PANEL, 214), UiKit.EDGE, 12);

        UiKit.shardIcon(g, x + 30, PAD + 30, 12, UiKit.SHARD);
        UiKit.text(g, "TIME SHARD", x + 50, PAD + 27, UiKit.bold(13), UiKit.TEXT_DIM);
        UiKit.text(g, level.shardsCollected() + " / " + level.totalShards(),
                x + 50, PAD + 49, UiKit.bold(18), UiKit.TEXT);

        int required = level.requiredShards();
        boolean enough = level.shardsCollected() >= required;
        UiKit.text(g, "Cần " + required + " để mở cổng", x + 150, PAD + 49,
                UiKit.regular(12), enough ? UiKit.OK : UiKit.TEXT_MUTED);

        UiKit.coinIcon(g, x + 30, PAD + 72, 10);
        UiKit.text(g, "x " + game.progress().coins(), x + 50, PAD + 77, UiKit.bold(15), UiKit.GOLD);

        UiKit.text(g, "MẠNG", x + 150, PAD + 77, UiKit.regular(12), UiKit.TEXT_MUTED);
        for (int i = 0; i < Game.LIVES_PER_LEVEL; i++) {
            UiKit.heartIcon(g, x + 200 + i * 22, PAD + 72, 9, i < lives);
        }
    }

    // ---- Giữa trên: tên màn, đồng hồ, thời đại ------------------------------
    private void renderCenter(Graphics2D g, Level level, Player p) {
        int cx = Game.VIEW_W / 2;

        UiKit.textCenter(g, level.name().toUpperCase(), cx, 40, UiKit.bold(20), UiKit.TEXT);
        UiKit.textCenter(g, UiKit.formatTime(level.elapsed()), cx, 62, UiKit.regular(14), UiKit.TEXT_DIM);

        TimeState state = level.timeState();
        String label = state.label();
        g.setFont(UiKit.bold(16));
        int tw = g.getFontMetrics().stringWidth(label);
        double pillW = tw + 54;
        double pillX = cx - pillW / 2;
        UiKit.panel(g, pillX, 72, pillW, 30, UiKit.alpha(state.accent(), 46),
                UiKit.alpha(state.accent(), 220), 15);
        g.setColor(state.accent());
        g.fillOval((int) pillX + 12, 82, 10, 10);
        UiKit.textCenter(g, label, cx + 6, 93, UiKit.bold(16), UiKit.TEXT);

        if (level.isTimeFrozen()) {
            int a = (int) (170 + 70 * Math.sin(pulse * 9));
            UiKit.textCenterShadow(g, "THỜI GIAN ĐÓNG BĂNG", cx, 132, UiKit.bold(18),
                    UiKit.alpha(new Color(0xCFEFFF), a), 2);
        }
        if (p != null && p.isRewinding()) {
            int a = (int) (150 + 90 * Math.sin(pulse * 16));
            UiKit.textCenterShadow(g, "TUA NGƯỢC", cx, 132, UiKit.bold(22),
                    UiKit.alpha(UiKit.ACCENT, a), 2);
        }
    }

    // ---- Góc dưới trái: năng lực ------------------------------------------
    private void renderAbilities(Graphics2D g, Game game, Player p) {
        int x = PAD;
        int y = Game.VIEW_H - PAD - 44;
        UiKit.panel(g, x, y, 470, 44, UiKit.alpha(UiKit.PANEL, 200), UiKit.EDGE, 12);

        int cx = x + 22;
        cx = ability(g, cx, y + 22, "SHIFT", "Dash", game.progress().hasDash());
        cx = ability(g, cx, y + 22, "Q", "Tua ngược", true);
        cx = ability(g, cx, y + 22, "F", "Đóng băng", game.progress().hasTimeFreeze());
        ability(g, cx, y + 22, "J", "Chém", true);
    }

    private int ability(Graphics2D g, int x, int cy, String key, String label, boolean unlocked) {
        Color c = unlocked ? UiKit.TEXT : UiKit.TEXT_MUTED;
        UiKit.drawKey(g, key, x, cy - 13);
        g.setFont(UiKit.bold(15));
        int kw = Math.max(30, g.getFontMetrics().stringWidth(key) + 16);
        UiKit.text(g, label, x + kw + 8, cy + 5, UiKit.regular(13), c);
        return x + kw + 12 + g.getFontMetrics(UiKit.regular(13)).stringWidth(label) + 18;
    }

    // ---- Thông báo nổi -----------------------------------------------------
    private void renderToasts(Graphics2D g, Game game) {
        List<Game.Toast> toasts = game.toasts();
        int y = 200;
        for (int i = toasts.size() - 1; i >= 0; i--) {
            Game.Toast t = toasts.get(i);
            int a = (int) (t.alpha() * 255);
            g.setFont(UiKit.bold(15));
            int tw = g.getFontMetrics().stringWidth(t.text()) + 30;
            UiKit.panel(g, Game.VIEW_W - PAD - tw, y, tw, 32,
                    UiKit.alpha(UiKit.PANEL, (int) (a * 0.85)), UiKit.alpha(UiKit.ACCENT, a), 9);
            UiKit.text(g, t.text(), Game.VIEW_W - PAD - tw + 15, y + 21,
                    UiKit.bold(15), UiKit.alpha(UiKit.TEXT, a));
            y += 38;
        }
    }

    // ---- Thanh máu trùm ----------------------------------------------------
    private void renderBoss(Graphics2D g, Level level) {
        Enemy boss = level.boss();
        if (boss == null) {
            return;
        }
        double w = 560;
        double x = (Game.VIEW_W - w) / 2;
        double y = Game.VIEW_H - 92;

        UiKit.textCenter(g, boss.displayName(), Game.VIEW_W / 2.0, y - 8,
                UiKit.bold(18), UiKit.alpha(UiKit.VIOLET, 230));
        UiKit.bar(g, x, y, w, 18, boss.healthRatio(), UiKit.DANGER,
                new Color(0x1A0D12), UiKit.alpha(UiKit.VIOLET, 200));
        UiKit.textCenter(g, boss.health() + " / " + boss.maxHealth(),
                Game.VIEW_W / 2.0, y + 34, UiKit.regular(13), UiKit.TEXT_DIM);
    }

    // ---- Gợi ý ngữ cảnh ----------------------------------------------------
    private void renderPrompt(Graphics2D g, Game game) {
        String prompt = game.prompt();
        if (prompt == null || prompt.isEmpty()) {
            return;
        }
        g.setFont(UiKit.bold(17));
        int tw = g.getFontMetrics().stringWidth(prompt) + 44;
        double x = (Game.VIEW_W - tw) / 2.0;
        double y = Game.VIEW_H - 168;
        UiKit.panel(g, x, y, tw, 38, UiKit.alpha(new Color(0x131A29), 226), UiKit.ACCENT, 12);
        UiKit.textCenter(g, prompt, Game.VIEW_W / 2.0, y + 25, UiKit.bold(17), UiKit.TEXT);
    }

    // ---- Dòng chữ lớn giữa màn hình ---------------------------------------
    private void renderBanner(Graphics2D g) {
        if (bannerTimer <= 0 || banner.isEmpty()) {
            return;
        }
        double t = Math.min(1, bannerTimer * 1.6);
        int a = (int) (255 * t);
        UiKit.textCenterShadow(g, banner, Game.VIEW_W / 2.0, Game.VIEW_H / 2.0 - 60,
                UiKit.bold(38), UiKit.alpha(bannerColor, a), 3);
    }

}
