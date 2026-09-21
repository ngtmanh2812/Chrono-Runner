package chronorunner.world;

import chronorunner.core.Game;
import chronorunner.core.Physics;
import chronorunner.core.TimeState;
import chronorunner.entity.Player;
import chronorunner.entity.boss.TemporalKnight;
import chronorunner.entity.boss.TheParadox;
import chronorunner.entity.enemy.ChronoBat;
import chronorunner.entity.enemy.TimeGuardian;
import chronorunner.entity.enemy.TimeSlime;
import chronorunner.item.ChronoCoin;
import chronorunner.item.HealthOrb;
import chronorunner.item.TimeCrystal;
import chronorunner.item.TimeShard;
import chronorunner.obstacle.CrusherTrap;
import chronorunner.obstacle.ExitPortal;
import chronorunner.obstacle.FallingPlatform;
import chronorunner.obstacle.MovingPlatform;
import chronorunner.obstacle.StaticPlatform;
import chronorunner.obstacle.TemporalPlatform;
import chronorunner.obstacle.TimeGate;
import chronorunner.util.Progress;

/**
 * Bộ dựng màn chơi theo mẫu Builder.
 *
 * <p>Thay vì viết tay một bản đồ ký tự khổng lồ, mỗi màn được mô tả bằng
 * chuỗi lệnh có ngữ nghĩa rõ ràng:</p>
 *
 * <pre>
 * LevelBuilder b = new LevelBuilder(game, 1, "Broken City", ...);
 * b.ground(0, 99, 20);
 * b.pit(16, 3).pit(30, 3);
 * b.bridge(48, 20, 8);         // 'X' - chỉ có ở QUÁ KHỨ và TƯƠNG LAI
 * b.gate(44, 19);
 * b.slime(22, 19);
 * b.shard(12, 18);
 * Level level = b.build();
 * </pre>
 *
 * <p>Phương thức {@link #stamp(String[], int, int)} cho phép dán thêm các
 * khối hình viết bằng ký tự - dùng để tạo cảnh trí phức tạp mà không phải
 * gõ tay từng ô.</p>
 */
public class LevelBuilder {

    private static final int TS = Physics.TILE_SIZE;

    private final Game game;
    private final int index;
    private final Level level;
    private final int width;
    private final int height;

    private double checkpointX = -1;
    private double checkpointY = -1;
    private boolean hasPlayer;
    private boolean hasExit;

    public LevelBuilder(Game game, int index, String name, String subtitle,
                        String intro, int widthTiles, int heightTiles) {
        this.game = game;
        this.index = index;
        this.width = widthTiles;
        this.height = heightTiles;
        this.level = new Level(game, index, name, subtitle, intro, widthTiles, heightTiles);
    }

    // ---- Địa hình ----------------------------------------------------------
    /** Tô một vùng ô. */
    public LevelBuilder fill(int tx, int ty, int tw, int th, TileType type) {
        for (int y = ty; y < ty + th; y++) {
            for (int x = tx; x < tx + tw; x++) {
                level.setTile(x, y, type);
            }
        }
        return this;
    }

    /** Xoá một vùng ô về trạng thái trống. */
    public LevelBuilder clear(int tx, int ty, int tw, int th) {
        return fill(tx, ty, tw, th, TileType.EMPTY);
    }

    /** Đổ đất từ hàng {@code topRow} xuống hết đáy màn. */
    public LevelBuilder ground(int x0, int x1, int topRow) {
        return fill(x0, topRow, x1 - x0 + 1, height - topRow, TileType.GROUND);
    }

    /** Một hàng ô. */
    public LevelBuilder row(int x0, int x1, int ty, TileType type) {
        return fill(x0, ty, x1 - x0 + 1, 1, type);
    }

    /** Một cột ô. */
    public LevelBuilder column(int tx, int y0, int y1, TileType type) {
        return fill(tx, y0, 1, y1 - y0 + 1, type);
    }

    /** Hố sâu (xoá đất tạo vực). */
    public LevelBuilder pit(int tx, int tilesWide) {
        return clear(tx, 0, tilesWide, height);
    }

    /** Bậc thang đi lên hoặc xuống. */
    public LevelBuilder stairs(int tx, int topRow, int steps, boolean ascending) {
        for (int i = 0; i < steps; i++) {
            int col = tx + i;
            int top = ascending ? topRow - i : topRow + i;
            ground(col, col, top);
        }
        return this;
    }

    /** Cầu nối ba thời đại: rắn ở QUÁ KHỨ và TƯƠNG LAI, đứt ở HIỆN TẠI. */
    public LevelBuilder bridge(int tx, int ty, int tilesWide) {
        return row(tx, tx + tilesWide - 1, ty, TileType.BRIDGE);
    }

    /** Bệ chỉ tồn tại ở QUÁ KHỨ. */
    public LevelBuilder pastPlatform(int tx, int ty, int tilesWide) {
        return row(tx, tx + tilesWide - 1, ty, TileType.PLATFORM_PAST);
    }

    /** Bệ chỉ tồn tại ở TƯƠNG LAI. */
    public LevelBuilder futurePlatform(int tx, int ty, int tilesWide) {
        return row(tx, tx + tilesWide - 1, ty, TileType.PLATFORM_FUTURE);
    }

    /** Dải băng trơn. */
    public LevelBuilder ice(int tx, int ty, int tilesWide) {
        return row(tx, tx + tilesWide - 1, ty, TileType.ICE);
    }

    /** Bãi gai. */
    public LevelBuilder spikes(int tx, int ty, int tilesWide) {
        return row(tx, tx + tilesWide - 1, ty, TileType.SPIKE);
    }

    /** Hồ dung nham. */
    public LevelBuilder lava(int tx, int ty, int tilesWide) {
        return row(tx, tx + tilesWide - 1, ty, TileType.LAVA);
    }

    /**
     * Dán một khối hình viết bằng ký tự vào bản đồ.
     *
     * <p>Ký tự '.', ' ' được bỏ qua (giữ nguyên ô hiện có); các ký tự còn lại
     * vừa có thể là ô địa hình, vừa có thể là vật thể (xem {@link TileType}).</p>
     *
     * @param rows các hàng ký tự
     * @param ox   toạ độ ô X của góc trên trái
     * @param oy   toạ độ ô Y của góc trên trái
     */
    public LevelBuilder stamp(String[] rows, int ox, int oy) {
        for (int r = 0; r < rows.length; r++) {
            String line = rows[r];
            for (int c = 0; c < line.length(); c++) {
                char ch = line.charAt(c);
                if (ch == '.' || ch == ' ') {
                    continue;
                }
                applyChar(ch, ox + c, oy + r);
            }
        }
        return this;
    }

    // ---- Vật thể -----------------------------------------------------------
    public LevelBuilder player(int tx, int ty) {
        Player p = new Player(tx * TS + 5, (ty + 1) * TS - 34.0, game.progress());
        level.setPlayer(p);
        hasPlayer = true;
        return this;
    }

    public LevelBuilder gate(int tx, int ty) {
        level.spawn(new TimeGate(tx * TS + TS / 2.0, (ty + 1) * TS));
        return this;
    }

    public LevelBuilder exit(int tx, int ty) {
        ExitPortal portal = new ExitPortal(tx * TS + TS / 2.0, (ty + 1) * TS);
        level.setExitPortal(portal);
        level.spawn(portal);
        hasExit = true;
        return this;
    }

    public LevelBuilder shard(int tx, int ty) {
        level.spawn(new TimeShard(tx * TS + 7, ty * TS + 7));
        level.registerShard();
        return this;
    }

    /** Một hàng Time Shard. */
    public LevelBuilder shards(int tx, int ty, int count) {
        for (int i = 0; i < count; i++) {
            shard(tx + i, ty);
        }
        return this;
    }

    public LevelBuilder crystal(int tx, int ty) {
        level.spawn(new TimeCrystal(tx * TS + 8, ty * TS + 8));
        return this;
    }

    public LevelBuilder health(int tx, int ty) {
        level.spawn(new HealthOrb(tx * TS + 8, ty * TS + 8));
        return this;
    }

    public LevelBuilder coin(int tx, int ty) {
        level.spawn(new ChronoCoin(tx * TS + 9, ty * TS + 9));
        return this;
    }

    /** Một hàng coin nằm ngang. */
    public LevelBuilder coins(int tx, int ty, int count) {
        for (int i = 0; i < count; i++) {
            coin(tx + i, ty);
        }
        return this;
    }

    /** Cung coin hình vòm (phần thưởng cho cú nhảy khó). */
    public LevelBuilder coinArc(int tx, int ty, int count) {
        for (int i = 0; i < count; i++) {
            double t = count == 1 ? 0.5 : i / (double) (count - 1);
            int lift = (int) Math.round(Math.sin(t * Math.PI) * 2);
            coin(tx + i, ty - lift);
        }
        return this;
    }

    public LevelBuilder slime(int tx, int ty) {
        level.spawn(new TimeSlime(tx * TS + 2, (ty + 1) * TS - 24.0));
        return this;
    }

    public LevelBuilder bat(int tx, int ty) {
        level.spawn(new ChronoBat(tx * TS + 1, ty * TS + 6));
        return this;
    }

    public LevelBuilder guardian(int tx, int ty) {
        level.spawn(new TimeGuardian(tx * TS + 1, (ty + 1) * TS - 40.0));
        return this;
    }

    public LevelBuilder knight(int tx, int ty) {
        level.spawn(new TemporalKnight(tx * TS, (ty + 1) * TS - 46.0));
        return this;
    }

    public LevelBuilder boss(int tx, int ty) {
        level.spawn(new TheParadox(tx * TS, (ty + 1) * TS - 64.0));
        return this;
    }

    public LevelBuilder movingPlatformH(int tx, int ty, int tilesWide, double rangeTiles, double speed) {
        level.spawn(MovingPlatform.horizontal(tx * TS, ty * TS, tilesWide * TS, 16,
                rangeTiles * TS, speed));
        return this;
    }

    public LevelBuilder movingPlatformV(int tx, int ty, int tilesWide, double rangeTiles, double speed) {
        level.spawn(MovingPlatform.vertical(tx * TS, ty * TS, tilesWide * TS, 16,
                rangeTiles * TS, speed));
        return this;
    }

    public LevelBuilder fallingPlatform(int tx, int ty, int tilesWide) {
        level.spawn(new FallingPlatform(tx * TS, ty * TS, tilesWide * TS, 16));
        return this;
    }

    public LevelBuilder staticPlatform(int tx, int ty, int tilesWide, int tilesHigh) {
        level.spawn(new StaticPlatform(tx * TS, ty * TS, tilesWide * TS, tilesHigh * TS));
        return this;
    }

    public LevelBuilder temporalPlatform(int tx, int ty, int tilesWide, int tilesHigh, int timeMask) {
        level.spawn(new TemporalPlatform(tx * TS, ty * TS, tilesWide * TS, tilesHigh * TS, timeMask));
        return this;
    }

    public LevelBuilder crusher(int tx, int ty, int tilesWide, double travelTiles) {
        level.spawn(new CrusherTrap(tx * TS, ty * TS, tilesWide * TS, 20, travelTiles * TS, 900));
        return this;
    }

    /** Điểm lưu: người chơi hồi sinh ở đây sau khi chết. */
    public LevelBuilder checkpoint(int tx, int ty) {
        checkpointX = tx * TS + 5;
        checkpointY = (ty + 1) * TS - 34.0;
        return this;
    }

    // ---- Kết thúc ----------------------------------------------------------
    public Level build() {
        if (checkpointX >= 0) {
            level.setCheckpoint(checkpointX, checkpointY);
        }
        if (!hasPlayer) {
            System.err.println("[LevelBuilder] Màn " + index + " thiếu điểm xuất phát của người chơi!");
        }
        if (!hasExit) {
            System.err.println("[LevelBuilder] Màn " + index + " thiếu cổng ra!");
        }
        if (level.totalShards() == 0) {
            System.err.println("[LevelBuilder] Màn " + index + " không có Time Shard nào!");
        }
        return level;
    }

    // ---- Nội bộ ------------------------------------------------------------
    private void applyChar(char ch, int tx, int ty) {
        switch (ch) {
            case 'S':
                player(tx, ty);
                return;
            case 'G':
                gate(tx, ty);
                return;
            case 'D':
                exit(tx, ty);
                return;
            case 'T':
                shard(tx, ty);
                return;
            case 'C':
                crystal(tx, ty);
                return;
            case 'H':
                health(tx, ty);
                return;
            case 'O':
                coin(tx, ty);
                return;
            case '1':
                slime(tx, ty);
                return;
            case '2':
                bat(tx, ty);
                return;
            case '3':
                guardian(tx, ty);
                return;
            case '4':
                knight(tx, ty);
                return;
            case 'B':
                boss(tx, ty);
                return;
            default:
                break;
        }

        TileType type = TileType.fromSymbol(ch);
        if (type != TileType.EMPTY) {
            level.setTile(tx, ty, type);
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Level level() {
        return level;
    }

    public TimeState initialTime() {
        return level.timeState();
    }
}
