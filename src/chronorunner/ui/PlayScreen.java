package chronorunner.ui;

import chronorunner.core.Camera;
import chronorunner.core.Game;
import chronorunner.core.Screen;
import chronorunner.entity.Player;
import chronorunner.world.Level;
import chronorunner.world.LevelFactory;

import java.awt.Graphics2D;

/**
 * Màn hình chơi chính: nạp một {@link Level}, chạy vòng cập nhật của nó
 * và vẽ thế giới qua camera.
 *
 * <p>Lớp này cũng là nơi duy nhất quản lý <b>mạng</b> của người chơi:
 * khi máu về 0, màn chơi tạm dừng trong giây lát rồi hồi sinh nhân vật
 * tại điểm lưu gần nhất; hết mạng thì chuyển sang màn hình kết quả.</p>
 */
public class PlayScreen implements Screen, Game.LevelListener {

    private static final double DEATH_PAUSE = 1.15;

    /** Khoảng nghỉ ngắn trước khi hiện bảng tổng kết, để người chơi kịp nhận ra. */
    private static final double COMPLETE_DELAY = 1.4;

    private final Level level;
    private final Hud hud = new Hud();

    private int lives = Game.LIVES_PER_LEVEL;
    private boolean dying;
    private double deathTimer;
    private boolean completing;
    private double completeTimer;

    public PlayScreen(Level level) {
        this.level = level;
    }

    public Level level() {
        return level;
    }

    @Override
    public void onEnter(Game game) {
        game.setLevel(level);
        game.setLevelListener(this);
        hud.setLives(lives);

        Player p = level.player();
        if (p != null) {
            game.camera().snapTo(p.centerX(), p.centerY(),
                    level.worldWidth(), level.worldHeight());
        }
        hud.banner(level.name().toUpperCase(), UiKit.ACCENT, 2.2);
    }

    @Override
    public void onExit(Game game) {
        game.setLevelListener(null);
    }

    // ---- Cập nhật ----------------------------------------------------------
    @Override
    public void update(Game game, double dt) {
        hud.update(dt);

        if (completing) {
            // Người chơi đã chạm cổng ra: cho màn chơi "lắng" lại rồi mới tổng kết.
            completeTimer -= dt;
            level.update(dt);
            game.camera().update(dt);
            if (completeTimer <= 0) {
                game.setScreen(new ResultScreen(level, true));
            }
            return;
        }

        if (game.input().pausePressed()) {
            game.pushScreen(new PauseScreen(level.index()));
            return;
        }
        if (game.input().restartPressed()) {
            game.setScreen(new PlayScreen(LevelFactory.create(game, level.index())));
            return;
        }

        Player p = level.player();
        level.update(dt);

        if (p != null) {
            game.camera().follow(p.centerX(), p.centerY(),
                    level.worldWidth(), level.worldHeight(), dt);

            if (!dying && !p.isAlive()) {
                onDeath(game);
            }
        }
        game.camera().update(dt);

        if (dying) {
            deathTimer -= dt;
            if (deathTimer <= 0) {
                if (lives > 0) {
                    dying = false;
                    level.respawnPlayer();
                    hud.banner("HỒI SINH", UiKit.OK, 1.3);
                } else {
                    game.setScreen(new ResultScreen(level, false));
                }
            }
        }
    }

    private void onDeath(Game game) {
        dying = true;
        deathTimer = DEATH_PAUSE;
        lives--;
        hud.setLives(lives);
        game.camera().shake(15, 0.7);
        hud.banner("THỜI GIAN ĐỨT ĐOẠN", UiKit.DANGER, 1.2);
        game.toast(lives > 0
                ? "Bạn đã gục ngã - còn " + lives + " mạng"
                : "Hết mạng rồi!");
    }

    // ---- Sự kiện từ màn chơi ----------------------------------------------
    @Override
    public void onLevelComplete() {
        if (completing) {
            return;
        }
        completing = true;
        completeTimer = COMPLETE_DELAY;
        hud.banner("CỔNG THỜI GIAN ĐÃ MỞ", UiKit.OK, COMPLETE_DELAY);
    }

    @Override
    public void onPlayerDied() {
        if (!dying && !completing) {
            onDeath(level.game());
        }
    }

    public boolean isCompleting() {
        return completing;
    }

    // ---- Vẽ ----------------------------------------------------------------
    @Override
    public void render(Game game, Graphics2D g) {
        Camera cam = game.camera();
        level.renderBackground(g, cam, Game.VIEW_W, Game.VIEW_H);

        Graphics2D world = (Graphics2D) g.create();
        cam.applyTo(world);
        level.render(world, cam);
        world.dispose();

        level.renderTimeOverlay(g, Game.VIEW_W, Game.VIEW_H);

        if (dying) {
            int a = (int) (60 + 70 * Math.min(1, deathTimer / DEATH_PAUSE));
            UiKit.scrim(g, Game.VIEW_W, Game.VIEW_H, a);
        }

        hud.render(g, game, level);
    }
}
