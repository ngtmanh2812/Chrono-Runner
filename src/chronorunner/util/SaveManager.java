package chronorunner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Ghi / đọc tiến trình người chơi ra tệp {@code .properties}.
 *
 * <p>Định dạng này được chọn vì có sẵn trong JDK, dễ đọc bằng mắt thường
 * và không cần thư viện ngoài.</p>
 */
public final class SaveManager {

    private static final String FILE_NAME = "chrono-save.properties";

    private SaveManager() {
    }

    /** Thư mục lưu game: {@code <thư mục chạy>/saves}. */
    public static File saveFile() {
        File dir = new File(System.getProperty("user.dir"), "saves");
        if (!dir.exists() && !dir.mkdirs()) {
            // Nếu không tạo được thư mục, ghi tạm ra thư mục hiện hành.
            return new File(System.getProperty("user.dir"), FILE_NAME);
        }
        return new File(dir, FILE_NAME);
    }

    public static void save(Progress progress) {
        Properties props = new Properties();
        props.setProperty("coins", String.valueOf(progress.coins()));
        for (Upgrade u : Upgrade.values()) {
            props.setProperty("upgrade." + u.name(), String.valueOf(progress.levelOf(u)));
        }
        props.setProperty("levels.unlocked", joinInts(progress.unlockedLevelsRaw()));
        props.setProperty("levels.completed", joinInts(progress.completedLevelsRaw()));
        for (Map.Entry<Integer, Integer> e : progress.bestShardsRaw().entrySet()) {
            props.setProperty("shards." + e.getKey(), String.valueOf(e.getValue()));
        }

        File target = saveFile();
        try (OutputStream out = new FileOutputStream(target)) {
            props.store(out, "Chrono Runner - save file");
        } catch (IOException ex) {
            System.err.println("[SaveManager] Không ghi được tệp lưu: " + ex.getMessage());
        }
    }

    public static Progress load() {
        Progress progress = new Progress();
        File source = saveFile();
        if (!source.isFile()) {
            return progress;
        }
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(source)) {
            props.load(in);
        } catch (IOException ex) {
            System.err.println("[SaveManager] Không đọc được tệp lưu: " + ex.getMessage());
            return progress;
        }

        progress.setCoins(parseInt(props.getProperty("coins"), 0));
        for (Upgrade u : Upgrade.values()) {
            int level = parseInt(props.getProperty("upgrade." + u.name()), 0);
            progress.setLevel(u, level);
        }

        progress.unlockedLevelsRaw().clear();
        for (int i : parseList(props.getProperty("levels.unlocked"))) {
            progress.unlockLevel(i);
        }
        if (progress.unlockedLevelsRaw().isEmpty()) {
            progress.unlockLevel(0);
        }
        for (int i : parseList(props.getProperty("levels.completed"))) {
            progress.completedLevelsRaw().add(i);
        }
        for (int i = 0; i < Progress.LEVEL_COUNT; i++) {
            String raw = props.getProperty("shards." + i);
            if (raw != null) {
                progress.bestShardsRaw().put(i, parseInt(raw, 0));
            }
        }
        return progress;
    }

    public static void deleteSave() {
        File f = saveFile();
        if (f.isFile() && !f.delete()) {
            System.err.println("[SaveManager] Không xoá được tệp lưu.");
        }
    }

    // ---- Tiện ích nội bộ ---------------------------------------------------
    private static String joinInts(Iterable<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (int v : values) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(v);
        }
        return sb.toString();
    }

    private static int[] parseList(String raw) {
        if (raw == null || raw.isBlank()) {
            return new int[0];
        }
        String[] parts = raw.split(",");
        int[] out = new int[parts.length];
        int n = 0;
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) {
                out[n++] = parseInt(t, -1);
            }
        }
        int[] trimmed = new int[n];
        System.arraycopy(out, 0, trimmed, 0, n);
        return trimmed;
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
