package chronorunner.util;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tiến trình chơi của người dùng: tiền, nâng cấp đã mua, màn đã mở.
 *
 * <p>Đây là lớp duy nhất nắm giữ trạng thái "vĩnh viễn" của trò chơi,
 * được {@code SaveManager} ghi ra / đọc vào từ tệp lưu.</p>
 */
public class Progress {

    /** Số màn của trò chơi. */
    public static final int LEVEL_COUNT = 5;

    // Giá trị hiệu dụng của từng nâng cấp, tra theo cấp độ đã mua.
    private static final double[] REWIND_SECONDS = {3.0, 5.0, 8.0, 12.0};
    private static final double[] MAX_ENERGY = {100, 140, 180, 220};
    private static final double[] DASH_COOLDOWN = {0.9, 0.75, 0.5};
    private static final double[] FREEZE_SECONDS = {0.0, 4.0, 7.0};

    private final EnumMap<Upgrade, Integer> upgradeLevels = new EnumMap<>(Upgrade.class);
    private final Set<Integer> unlockedLevels = new HashSet<>();
    private final Set<Integer> completedLevels = new HashSet<>();
    private final Map<Integer, Integer> bestShards = new java.util.HashMap<>();

    private int coins;

    public Progress() {
        for (Upgrade u : Upgrade.values()) {
            upgradeLevels.put(u, 0);
        }
        unlockedLevels.add(0);
    }

    // ---- Tiền tệ -----------------------------------------------------------
    public int coins() {
        return coins;
    }

    public void addCoins(int amount) {
        coins = Math.max(0, coins + amount);
    }

    public boolean canAfford(int amount) {
        return coins >= amount;
    }

    public boolean spend(int amount) {
        if (!canAfford(amount)) {
            return false;
        }
        coins -= amount;
        return true;
    }

    // ---- Nâng cấp ----------------------------------------------------------
    public int levelOf(Upgrade u) {
        return upgradeLevels.getOrDefault(u, 0);
    }

    public boolean isMaxed(Upgrade u) {
        return levelOf(u) >= u.maxLevel();
    }

    /** Giá của cấp kế tiếp, hoặc -1 nếu đã tối đa. */
    public int nextCost(Upgrade u) {
        if (isMaxed(u)) {
            return -1;
        }
        return u.costAt(levelOf(u));
    }

    public boolean isUnlocked(Upgrade u) {
        return levelOf(u) > 0;
    }

    /** Mua cấp kế tiếp. Trả về {@code true} nếu giao dịch thành công. */
    public boolean purchase(Upgrade u) {
        int cost = nextCost(u);
        if (cost < 0 || !canAfford(cost)) {
            return false;
        }
        coins -= cost;
        upgradeLevels.put(u, levelOf(u) + 1);
        return true;
    }

    public void setLevel(Upgrade u, int level) {
        upgradeLevels.put(u, Collision.clampInt(level, 0, u.maxLevel()));
    }

    // ---- Chỉ số hiệu dụng --------------------------------------------------
    public double rewindSeconds() {
        return REWIND_SECONDS[Collision.clampInt(levelOf(Upgrade.REWIND_DURATION), 0, REWIND_SECONDS.length - 1)];
    }

    public double maxTimeEnergy() {
        return MAX_ENERGY[Collision.clampInt(levelOf(Upgrade.TIME_ENERGY), 0, MAX_ENERGY.length - 1)];
    }

    public boolean hasDash() {
        return isUnlocked(Upgrade.DASH);
    }

    public double dashCooldown() {
        return DASH_COOLDOWN[Collision.clampInt(levelOf(Upgrade.DASH), 0, DASH_COOLDOWN.length - 1)];
    }

    public boolean hasDoubleJump() {
        return isUnlocked(Upgrade.DOUBLE_JUMP);
    }

    public boolean hasTimeFreeze() {
        return isUnlocked(Upgrade.TIME_FREEZE);
    }

    public double freezeSeconds() {
        return FREEZE_SECONDS[Collision.clampInt(levelOf(Upgrade.TIME_FREEZE), 0, FREEZE_SECONDS.length - 1)];
    }

    // ---- Tiến trình màn ----------------------------------------------------
    public boolean isLevelUnlocked(int index) {
        return unlockedLevels.contains(index);
    }

    public void unlockLevel(int index) {
        if (index >= 0 && index < LEVEL_COUNT) {
            unlockedLevels.add(index);
        }
    }

    public boolean isLevelCompleted(int index) {
        return completedLevels.contains(index);
    }

    public void markCompleted(int index, int shards, int totalShards) {
        if (index < 0) {
            return;
        }
        completedLevels.add(index);
        bestShards.merge(index, shards, Math::max);
        unlockLevel(index + 1);
        unlockLevel(index);
    }

    public int bestShards(int index) {
        return bestShards.getOrDefault(index, 0);
    }

    /** Tổng số Time Shard đã thu thập trong toàn bộ các màn. */
    public int totalShards() {
        int sum = 0;
        for (int v : bestShards.values()) {
            sum += v;
        }
        return sum;
    }

    public int completedCount() {
        return completedLevels.size();
    }

    // ---- Lưu trữ thô (SaveManager dùng) -------------------------------------
    public Set<Integer> unlockedLevelsRaw() {
        return unlockedLevels;
    }

    public Set<Integer> completedLevelsRaw() {
        return completedLevels;
    }

    public Map<Integer, Integer> bestShardsRaw() {
        return bestShards;
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public void reset() {
        for (Upgrade u : Upgrade.values()) {
            upgradeLevels.put(u, 0);
        }
        unlockedLevels.clear();
        unlockedLevels.add(0);
        completedLevels.clear();
        bestShards.clear();
        coins = 0;
    }
}
