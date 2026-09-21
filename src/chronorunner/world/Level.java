package chronorunner.world;

import chronorunner.core.Camera;
import chronorunner.core.Game;
import chronorunner.core.InputHandler;
import chronorunner.core.Physics;
import chronorunner.core.TimeManager;
import chronorunner.core.TimeState;
import chronorunner.entity.FloatingText;
import chronorunner.entity.GameObject;
import chronorunner.entity.Particle;
import chronorunner.entity.Player;
import chronorunner.entity.Projectile;
import chronorunner.entity.enemy.Enemy;
import chronorunner.item.ChronoCoin;
import chronorunner.item.Item;
import chronorunner.obstacle.ExitPortal;
import chronorunner.obstacle.Obstacle;
import chronorunner.obstacle.Platform;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

/**
 * Một màn chơi hoàn chỉnh: bản đồ ô, danh sách vật thể và trục thời gian.
 *
 * <p>Level là "thế giới" mà mọi vật thể tương tác vào. Nó cung cấp:</p>
 * <ul>
 *   <li>truy vấn va chạm với bản đồ ({@link #solidInRect}, {@link #hazardAt}),</li>
 *   <li>quản lý thời đại và đóng băng qua {@link TimeManager},</li>
 *   <li>vòng cập nhật / vẽ theo đúng thứ tự lớp,</li>
 *   <li>các bộ đếm tiến trình màn (Time Shard, coin).</li>
 * </ul>
 */
public class Level {

    private final Game game;
    private final int index;
    private final String name;
    private final String subtitle;
    private final String intro;

    private final int widthTiles;
    private final int heightTiles;
    private final TileType[][] tiles;

    private final TimeManager timeManager = new TimeManager();

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<GameObject> effects = new ArrayList<>();

    private Player player;
    private ExitPortal exitPortal;

    private int shardsCollected;
    private int totalShards;
    private int coinsCollected;
    private double elapsed;

    /** Điểm lưu tạm khi người chơi chết giữa màn. */
    private double checkpointX = -1;
    private double checkpointY = -1;

    /** Vị trí xuất phát ban đầu của người chơi. */
    private double spawnX;
    private double spawnY;

    public Level(Game game, int index, String name, String subtitle, String intro,
                 int widthTiles, int heightTiles) {
        this.game = game;
        this.index = index;
        this.name = name;
        this.subtitle = subtitle;
        this.intro = intro;
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.tiles = new TileType[heightTiles][widthTiles];
        for (int ty = 0; ty < heightTiles; ty++) {
            for (int tx = 0; tx < widthTiles; tx++) {
                tiles[ty][tx] = TileType.EMPTY;
            }
        }
    }

    // ---- Truy cập cơ bản ---------------------------------------------------
    public Game game() {
        return game;
    }

    public InputHandler input() {
        return game.input();
    }

    public Camera camera() {
        return game.camera();
    }

    public int index() {
        return index;
    }

    public String name() {
        return name;
    }

    public String subtitle() {
        return subtitle;
    }

    public String intro() {
        return intro;
    }

    public int tileSize() {
        return Physics.TILE_SIZE;
    }

    public int widthTiles() {
        return widthTiles;
    }

    public int heightTiles() {
        return heightTiles;
    }

    public double worldWidth() {
        return widthTiles * (double) tileSize();
    }

    public double worldHeight() {
        return heightTiles * (double) tileSize();
    }

    public Player player() {
        return player;
    }

    public TimeManager timeManager() {
        return timeManager;
    }

    public TimeState timeState() {
        return timeManager.current();
    }

    public boolean isTimeFrozen() {
        return timeManager.isFrozen();
    }

    public List<Platform> platforms() {
        return platforms;
    }

    public List<Enemy> enemies() {
        return enemies;
    }

    public List<Projectile> projectiles() {
        return projectiles;
    }

    public List<Item> items() {
        return items;
    }

    public ExitPortal exitPortal() {
        return exitPortal;
    }

    public double elapsed() {
        return elapsed;
    }

    // ---- Tiến trình màn ----------------------------------------------------
    public int shardsCollected() {
        return shardsCollected;
    }

    public int totalShards() {
        return totalShards;
    }

    public int coinsCollected() {
        return coinsCollected;
    }

    public void addShard() {
        shardsCollected++;
    }

    public void addCoinCollected() {
        coinsCollected++;
    }

    public void registerShard() {
        totalShards++;
    }

    /** Số Time Shard tối thiểu để mở cổng ra (60% tổng số). */
    public int requiredShards() {
        return Math.max(1, (int) Math.ceil(totalShards * 0.6));
    }

    public void setExitPortal(ExitPortal portal) {
        this.exitPortal = portal;
    }

    public void setPlayer(Player p) {
        this.player = p;
        if (p != null) {
            this.spawnX = p.getX();
            this.spawnY = p.getY();
            timeManager.register(p);
        }
    }

    /**
     * Hồi sinh người chơi tại điểm lưu gần nhất (hoặc vị trí xuất phát).
     * Đạn đang bay được dọn sạch để người chơi không bị bắn ngay khi vừa sống lại.
     */
    public void respawnPlayer() {
        if (player == null) {
            return;
        }
        double rx = hasCheckpoint() ? checkpointX : spawnX;
        double ry = hasCheckpoint() ? checkpointY : spawnY;

        player.reviveAt(rx, ry);
        projectiles.clear();
        player.addTimeEnergy(player.maxTimeEnergy());
        camera().snapTo(player.centerX(), player.centerY(), worldWidth(), worldHeight());
    }

    public void setCheckpoint(double x, double y) {
        this.checkpointX = x;
        this.checkpointY = y;
    }

    public boolean hasCheckpoint() {
        return checkpointX >= 0;
    }

    public double checkpointX() {
        return checkpointX;
    }

    public double checkpointY() {
        return checkpointY;
    }

    // ---- Bản đồ ------------------------------------------------------------
    public void setTile(int tx, int ty, TileType type) {
        if (tx >= 0 && ty >= 0 && tx < widthTiles && ty < heightTiles) {
            tiles[ty][tx] = type;
        }
    }

    public TileType tileAt(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= widthTiles || ty >= heightTiles) {
            return TileType.EMPTY;
        }
        return tiles[ty][tx];
    }

    /** Có ô rắn nào giao với hình chữ nhật đang xét (theo thời đại hiện tại)? */
    public boolean solidInRect(double x, double y, double w, double h) {
        int ts = tileSize();
        int x0 = (int) Math.floor(x / ts);
        int x1 = (int) Math.floor((x + w) / ts);
        int y0 = (int) Math.floor(y / ts);
        int y1 = (int) Math.floor((y + h) / ts);
        for (int ty = y0; ty <= y1; ty++) {
            for (int tx = x0; tx <= x1; tx++) {
                if (tileAt(tx, ty).solidIn(timeState())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Ô nguy hiểm đầu tiên giao với hình chữ nhật, hoặc {@code null}. */
    public TileType hazardAt(double x, double y, double w, double h) {
        int ts = tileSize();
        int x0 = (int) Math.floor(x / ts);
        int x1 = (int) Math.floor((x + w) / ts);
        int y0 = (int) Math.floor(y / ts);
        int y1 = (int) Math.floor((y + h) / ts);
        for (int ty = y0; ty <= y1; ty++) {
            for (int tx = x0; tx <= x1; tx++) {
                TileType t = tileAt(tx, ty);
                if (t.isHazard()) {
                    return t;
                }
            }
        }
        return null;
    }

    /** Nhân vật có đang đứng trên băng không (màn 4). */
    public boolean isOnIce(double x, double y, double w, double h) {
        int ts = tileSize();
        int x0 = (int) Math.floor(x / ts);
        int x1 = (int) Math.floor((x + w) / ts);
        int ty = (int) Math.floor((y + h + 2) / ts);
        for (int tx = x0; tx <= x1; tx++) {
            if (tileAt(tx, ty) == TileType.ICE) {
                return true;
            }
        }
        return false;
    }

    // ---- Sinh / huỷ vật thể ------------------------------------------------
    /** Thêm vật thể mới vào đúng danh sách theo kiểu của nó. */
    public void spawn(GameObject object) {
        if (object == null) {
            return;
        }
        if (object instanceof Enemy) {
            enemies.add((Enemy) object);
        } else if (object instanceof Projectile) {
            projectiles.add((Projectile) object);
        } else if (object instanceof Item) {
            items.add((Item) object);
        } else if (object instanceof Platform) {
            platforms.add((Platform) object);
        } else if (object instanceof Obstacle) {
            obstacles.add((Obstacle) object);
        } else {
            effects.add(object);
        }
        timeManager.register(object);
    }

    /** Xoá một vật thể khỏi màn chơi. */
    public void despawn(GameObject object) {
        platforms.remove(object);
        obstacles.remove(object);
        items.remove(object);
        projectiles.remove(object);
        enemies.remove(object);
        effects.remove(object);
        timeManager.unregister(object);
        object.kill();
    }

    /** Bệ động đang hoạt động (dùng cho HUD gợi ý puzzle). */
    public List<Platform> temporalPlatforms() {
        List<Platform> out = new ArrayList<>();
        for (Platform p : platforms) {
            if ((p.getTimeMask() & TimeState.ALL) != TimeState.ALL) {
                out.add(p);
            }
        }
        return out;
    }

    /** Trùm đang hoạt động, hoặc {@code null}. */
    public Enemy boss() {
        for (Enemy e : enemies) {
            if (e.isBoss() && e.isAlive()) {
                return e;
            }
        }
        return null;
    }

    // ---- Thời gian ---------------------------------------------------------
    public void shiftTime(TimeState to) {
        if (timeManager.shiftTo(to)) {
            game.camera().shake(6, 0.3);
            for (int i = 0; i < 30; i++) {
                double ang = Math.random() * Math.PI * 2;
                spawn(new Particle(player == null ? worldWidth() / 2 : player.centerX(),
                        player == null ? worldHeight() / 2 : player.centerY(),
                        Math.cos(ang) * 220, Math.sin(ang) * 220,
                        to.accent(), 0.7, 5, false));
            }
            game.toast("Thời đại: " + to.label());
        }
    }

    public void freezeTime(double seconds) {
        timeManager.freeze(seconds);
        game.camera().shake(7, 0.4);
    }

    public void clearFreeze() {
        timeManager.clearFreeze();
    }

    // ---- Vòng cập nhật -----------------------------------------------------
    public void update(double dt) {
        elapsed += dt;
        timeManager.update(dt);

        boolean frozen = timeManager.isFrozen();

        for (Platform p : platforms) {
            if (frozen) {
                p.clearDelta();
            } else {
                p.update(this, dt);
            }
        }
        for (Obstacle o : obstacles) {
            o.update(this, dt);
        }
        for (Item it : items) {
            it.update(this, dt);
        }
        for (Projectile p : projectiles) {
            p.update(this, dt);
        }
        for (Enemy e : enemies) {
            e.update(this, dt);
        }
        if (player != null) {
            player.update(this, dt);
        }
        for (GameObject fx : effects) {
            fx.update(this, dt);
        }

        prune();
    }

    private void prune() {
        platforms.removeIf(o -> !o.isAlive());
        obstacles.removeIf(o -> !o.isAlive());
        items.removeIf(o -> !o.isAlive());
        projectiles.removeIf(o -> !o.isAlive());
        enemies.removeIf(o -> !o.isAlive());
        effects.removeIf(o -> !o.isAlive());

        // Bỏ đăng ký các vật thể đã chết khỏi TimeManager.
        timeManager.unregisterIfDead();
    }

    // ---- Vẽ ----------------------------------------------------------------
    /** Vẽ nền trời và các lớp xa (parallax). Gọi TRƯỚC khi dịch camera. */
    public void renderBackground(Graphics2D g, Camera cam, int viewW, int viewH) {
        TimeState state = timeState();
        java.awt.GradientPaint sky = new java.awt.GradientPaint(
                0, 0, state.skyTop(), 0, viewH, state.skyBottom());
        g.setPaint(sky);
        g.fillRect(0, 0, viewW, viewH);

        // Sao / bụi thời gian
        g.setColor(new Color(255, 255, 255, 26));
        for (int i = 0; i < 70; i++) {
            double sx = (i * 197.3 + cam.getX() * 0.08) % viewW;
            double sy = (i * 91.7) % (viewH * 0.7);
            g.fillRect((int) sx, (int) sy, 2, 2);
        }

        // Silhouette thành phố / rừng / phế tích ở lớp xa
        double parallax = cam.getX() * 0.35;
        g.setColor(darker(state.skyBottom(), 0.55f));
        int baseY = viewH - 80;
        for (int i = -1; i < 26; i++) {
            double bx = i * 180 - (parallax % 180);
            int bw = 90 + (i * 37 % 50);
            int bh = 90 + (Math.abs(i * 53) % 160);
            g.fillRect((int) bx, baseY - bh, bw, bh);
            if (state == TimeState.FUTURE) {
                g.setColor(new Color(state.accent().getRed(), state.accent().getGreen(),
                        state.accent().getBlue(), 40));
                g.fillRect((int) bx + 12, baseY - bh + 16, 16, bh - 30);
                g.setColor(darker(state.skyBottom(), 0.55f));
            }
        }
    }

    private static Color darker(Color c, float factor) {
        return new Color(
                Math.max(0, (int) (c.getRed() * factor)),
                Math.max(0, (int) (c.getGreen() * factor)),
                Math.max(0, (int) (c.getBlue() * factor)));
    }

    /** Vẽ toàn bộ nội dung màn chơi. Camera đã được dịch trước khi gọi. */
    public void render(Graphics2D g, Camera cam) {
        renderTiles(g, cam);

        for (Obstacle o : obstacles) {
            if (o instanceof ExitPortal || o instanceof chronorunner.obstacle.TimeGate) {
                o.render(g);
            }
        }
        for (Platform p : platforms) {
            renderObject(g, cam, p);
        }
        for (Item it : items) {
            renderObject(g, cam, it);
        }
        for (Obstacle o : obstacles) {
            if (!(o instanceof ExitPortal) && !(o instanceof chronorunner.obstacle.TimeGate)) {
                if (o.existsIn(timeState())) {
                    o.render(g);
                }
            }
        }
        for (Enemy e : enemies) {
            renderObject(g, cam, e);
        }
        if (player != null && player.isAlive() && player.existsIn(timeState())) {
            player.render(g, this);
        }
        for (Projectile p : projectiles) {
            p.render(g);
        }
        for (GameObject fx : effects) {
            fx.render(g);
        }
    }

    private void renderObject(Graphics2D g, Camera cam, GameObject o) {
        if (!o.isVisible() || !o.isAlive()) {
            return;
        }
        // Bệ thời gian tự vẽ trạng thái "biến mất", các vật thể khác thì ẩn hẳn.
        if (!o.existsIn(timeState()) && !(o instanceof Platform)) {
            return;
        }
        if (!o.isNear(cam.getX() + cam.viewWidth() / 2.0,
                cam.getY() + cam.viewHeight() / 2.0,
                cam.viewWidth() * 0.75 + 200)) {
            return;
        }
        o.render(g);
    }

    private void renderTiles(Graphics2D g, Camera cam) {
        int ts = tileSize();
        int x0 = Math.max(0, (int) (cam.getX() / ts) - 1);
        int x1 = Math.min(widthTiles - 1, (int) ((cam.getX() + cam.viewWidth()) / ts) + 1);
        int y0 = Math.max(0, (int) (cam.getY() / ts) - 1);
        int y1 = Math.min(heightTiles - 1, (int) ((cam.getY() + cam.viewHeight()) / ts) + 1);

        TimeState state = timeState();

        for (int ty = y0; ty <= y1; ty++) {
            for (int tx = x0; tx <= x1; tx++) {
                TileType t = tiles[ty][tx];
                if (t == TileType.EMPTY) {
                    continue;
                }
                int px = tx * ts;
                int py = ty * ts;

                if (t == TileType.SPIKE) {
                    g.setColor(new Color(0x8E2B22));
                    g.fillRect(px, py + ts - 6, ts, 6);
                    g.setColor(new Color(0xD94F3D));
                    for (int i = 0; i < 4; i++) {
                        int sx = px + i * (ts / 4);
                        g.fillPolygon(new Polygon(
                                new int[]{sx, sx + ts / 8, sx + ts / 4},
                                new int[]{py + ts, py + 4, py + ts}, 3));
                    }
                    continue;
                }

                if (t == TileType.LAVA) {
                    g.setColor(new Color(0x7A1F0A));
                    g.fillRect(px, py, ts, ts);
                    int wave = (int) (Math.sin(elapsed * 3 + tx * 0.7) * 3);
                    g.setColor(new Color(0xE8622A));
                    g.fillRect(px, py + 4 + wave, ts, ts - 4);
                    g.setColor(new Color(0xFFC46B));
                    g.fillRect(px, py + 4 + wave, ts, 3);
                    continue;
                }

                if (!t.solidIn(state)) {
                    if (t.solidAnyTime()) {
                        // Ô rắn nhưng không tồn tại ở thời đại này -> viền mờ
                        g.setColor(new Color(t.colorFor(state).getRed(),
                                t.colorFor(state).getGreen(),
                                t.colorFor(state).getBlue(), 45));
                        g.fillRect(px + 2, py + 2, ts - 4, ts - 4);
                        g.setColor(new Color(255, 255, 255, 60));
                        g.drawRect(px + 2, py + 2, ts - 5, ts - 5);
                    }
                    continue;
                }

                Color base = t.colorFor(state);
                g.setColor(base);
                g.fillRect(px, py, ts, ts);

                // Viền trên sáng khi phía trên trống
                if (!tileAt(tx, ty - 1).solidIn(state)) {
                    g.setColor(new Color(
                            Math.min(255, base.getRed() + 55),
                            Math.min(255, base.getGreen() + 55),
                            Math.min(255, base.getBlue() + 55)));
                    g.fillRect(px, py, ts, 4);
                }

                g.setColor(new Color(0, 0, 0, 55));
                g.drawRect(px, py, ts - 1, ts - 1);

                // Chi tiết nhỏ cho đỡ đơn điệu
                if (((tx * 7 + ty * 13) % 5) == 0) {
                    g.setColor(new Color(0, 0, 0, 40));
                    g.fillRect(px + 6, py + 10, 6, 6);
                    g.fillRect(px + 20, py + 20, 5, 5);
                }
            }
        }
    }

    /** Lớp phủ màu theo thời đại + hiệu ứng đóng băng. Gọi sau khi vẽ xong. */
    public void renderTimeOverlay(Graphics2D g, int viewW, int viewH) {
        TimeState state = timeState();
        Color tint = state.accent();
        Composite old = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.07f));
        g.setColor(tint);
        g.fillRect(0, 0, viewW, viewH);

        if (timeManager.isFrozen()) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
            g.setColor(new Color(0x9FE8FF));
            g.fillRect(0, 0, viewW, viewH);

            // Vài tinh thể băng rơi
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setColor(new Color(0xFFFFFF));
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(2f));
            for (int i = 0; i < 12; i++) {
                int fx = (int) ((i * 137 + elapsed * 30) % viewW);
                int fy = (int) ((i * 211 + elapsed * 90) % viewH);
                g.drawLine(fx, fy, fx, fy + 12);
                g.drawLine(fx - 5, fy + 4, fx + 5, fy + 4);
            }
            g.setStroke(oldStroke);
        }

        g.setComposite(old);
    }

    /** Nhắc người chơi bằng chữ nổi trong thế giới (dùng cho hướng dẫn). */
    public void hint(double worldX, double worldY, String text, Color color) {
        FloatingText ft = new FloatingText(worldX, worldY, text, color, 1.4);
        ft.setVelocity(0, -18);
        spawn(ft);
    }

    /** Thưởng coin trực tiếp (dùng cho phần thưởng tiêu diệt trùm). */
    public void rewardCoins(double x, double y, int amount) {
        for (int i = 0; i < amount; i++) {
            spawn(new ChronoCoin(x + (Math.random() - 0.5) * 60, y - 10));
        }
    }
}
