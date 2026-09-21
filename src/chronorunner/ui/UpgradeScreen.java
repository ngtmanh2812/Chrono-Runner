package chronorunner.ui;

import chronorunner.core.Game;
import chronorunner.core.Screen;
import chronorunner.entity.Player;
import chronorunner.util.Upgrade;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

/**
 * Cửa hàng nâng cấp Chrono Core.
 *
 * <p>Màn hình duyệt qua {@link Upgrade#values()} nên khi thêm một nâng cấp
 * mới vào enum, giao diện tự động có thêm một dòng mà không cần sửa gì ở đây.</p>
 */
public class UpgradeScreen implements Screen {

    private static final double ROW_X = 168;
    private static final double ROW_W = 944;
    private static final double ROW_H = 78;
    private static final double ROW_STEP = 88;
    private static final double ROW_TOP = 188;

    private final Map<Upgrade, UiKit.Button> buyButtons = new EnumMap<>(Upgrade.class);
    private UiKit.Button backButton;
    private double time;
    private String flash;
    private double flashTimer;
    private Color flashColor = UiKit.OK;

    @Override
    public void onEnter(Game game) {
        double y = ROW_TOP;
        for (Upgrade u : Upgrade.values()) {
            UiKit.Button b = UiKit.button("MUA", ROW_X + ROW_W - 132, y + 13, 108, 52);
            b.action(() -> purchase(game, u));
            buyButtons.put(u, b);
            y += ROW_STEP;
        }
        backButton = UiKit.button("QUAY LẠI", Game.VIEW_W / 2.0 - 110, 646, 220, 52)
                .action(() -> game.popScreen());
    }

    private void purchase(Game game, Upgrade u) {
        int cost = game.progress().nextCost(u);
        if (cost < 0) {
            flash = u.displayName() + " đã đạt cấp tối đa";
            flashColor = UiKit.TEXT_DIM;
        } else if (!game.progress().purchase(u)) {
            flash = "Không đủ Chrono Coin (cần " + cost + ")";
            flashColor = UiKit.DANGER;
        } else {
            flash = "Đã nâng cấp " + u.displayName() + " lên cấp "
                    + game.progress().levelOf(u);
            flashColor = UiKit.OK;
            Player p = game.level() == null ? null : game.level().player();
            if (p != null) {
                p.refreshUpgrades();
            }
        }
        flashTimer = 2.4;
        game.save();
    }

    @Override
    public void update(Game game, double dt) {
        time += dt;
        if (flashTimer > 0) {
            flashTimer -= dt;
        }

        for (Upgrade u : Upgrade.values()) {
            UiKit.Button b = buyButtons.get(u);
            int cost = game.progress().nextCost(u);
            b.setEnabled(cost >= 0 && game.progress().canAfford(cost));
            b.setLabel(cost < 0 ? "TỐI ĐA" : String.valueOf(cost));
            UiKit.update(b, game.input());
        }

        UiKit.update(backButton, game.input());
        if (game.input().pausePressed() || game.input().confirmPressed()) {
            game.popScreen();
        }
    }

    @Override
    public void render(Game game, Graphics2D g) {
        UiKit.verticalGradient(g, Game.VIEW_W, Game.VIEW_H, new Color(0x0A1526), UiKit.BACKDROP);
        UiKit.starfield(g, Game.VIEW_W, Game.VIEW_H, time, UiKit.ACCENT);
        UiKit.vignette(g, Game.VIEW_W, Game.VIEW_H);

        UiKit.textCenterShadow(g, "CHRONO CORE", Game.VIEW_W / 2.0, 92,
                UiKit.bold(38), UiKit.TEXT, 2);
        UiKit.textCenter(g, "Nâng cấp sức mạnh của cỗ máy thời gian",
                Game.VIEW_W / 2.0, 122, UiKit.regular(15), UiKit.TEXT_DIM);

        // Ví coin
        UiKit.panel(g, Game.VIEW_W / 2.0 - 150, 140, 300, 42,
                UiKit.alpha(UiKit.PANEL, 225), UiKit.alpha(UiKit.GOLD, 160), 12);
        UiKit.coinIcon(g, Game.VIEW_W / 2.0 - 118, 161, 11);
        UiKit.text(g, "CHRONO COIN", Game.VIEW_W / 2.0 - 98, 166, UiKit.bold(13), UiKit.TEXT_DIM);
        UiKit.textRight(g, String.valueOf(game.progress().coins()),
                Game.VIEW_W / 2.0 + 128, 168, UiKit.bold(20), UiKit.GOLD);

        double y = ROW_TOP;
        for (Upgrade u : Upgrade.values()) {
            drawRow(g, game, u, y);
            y += ROW_STEP;
        }

        if (flashTimer > 0 && flash != null) {
            UiKit.textCenter(g, flash, Game.VIEW_W / 2.0, 634,
                    UiKit.bold(15), UiKit.alpha(flashColor, (int) (255 * Math.min(1, flashTimer))));
        }

        UiKit.draw(g, backButton, UiKit.ACCENT);
    }

    private void drawRow(Graphics2D g, Game game, Upgrade u, double y) {
        int level = game.progress().levelOf(u);
        boolean maxed = game.progress().isMaxed(u);
        Color accent = maxed ? UiKit.OK : (level > 0 ? UiKit.ACCENT : UiKit.TEXT_MUTED);

        UiKit.panel(g, ROW_X, y, ROW_W, ROW_H,
                UiKit.alpha(level > 0 ? UiKit.PANEL_LIGHT : UiKit.PANEL, 226),
                UiKit.alpha(accent, 120), 14);

        // Nút tròn biểu tượng
        g.setColor(UiKit.alpha(accent, 46));
        g.fillOval((int) ROW_X + 16, (int) y + 15, 48, 48);
        g.setColor(UiKit.alpha(accent, 220));
        g.drawOval((int) ROW_X + 16, (int) y + 15, 48, 48);
        UiKit.clockRing(g, ROW_X + 40, y + 39, 15, time * (0.7 + level * 0.3), accent, 2f);

        UiKit.text(g, u.displayName(), ROW_X + 84, y + 32, UiKit.bold(19), UiKit.TEXT);
        UiKit.text(g, u.description(), ROW_X + 84, y + 56, UiKit.regular(14), UiKit.TEXT_DIM);

        // Chấm cấp độ
        double px = ROW_X + 470;
        for (int i = 0; i < u.maxLevel(); i++) {
            boolean filled = i < level;
            g.setColor(filled ? accent : UiKit.alpha(UiKit.TEXT_MUTED, 90));
            g.fillOval((int) px + i * 22, (int) y + 34, 12, 12);
            if (filled) {
                g.setColor(UiKit.alpha(Color.WHITE, 150));
                g.drawOval((int) px + i * 22, (int) y + 34, 12, 12);
            }
        }
        UiKit.text(g, Upgrade.levelText(level, u.maxLevel()), px, y + 66,
                UiKit.regular(12), UiKit.TEXT_MUTED);

        // Giá
        int cost = game.progress().nextCost(u);
        if (maxed) {
            UiKit.textRight(g, "TỐI ĐA", ROW_X + ROW_W - 152, y + 44, UiKit.bold(15), UiKit.OK);
        } else {
            UiKit.textRight(g, String.valueOf(cost), ROW_X + ROW_W - 152, y + 44,
                    UiKit.bold(17), game.progress().canAfford(cost) ? UiKit.GOLD : UiKit.DANGER);
            UiKit.coinIcon(g, ROW_X + ROW_W - 136, y + 38, 8);
        }

        UiKit.draw(g, buyButtons.get(u), UiKit.GOLD, maxed ? UiKit.OK : null);
    }
}
