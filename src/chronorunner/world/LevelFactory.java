package chronorunner.world;

import chronorunner.core.Game;
import chronorunner.core.Physics;
import chronorunner.core.TimeState;
import chronorunner.entity.boss.TheParadox;

/**
 * Nhà máy sản xuất 5 màn chơi của Chrono Runner.
 *
 * <p>Toàn bộ bản đồ được mô tả bằng các lời gọi ngữ nghĩa của
 * {@link LevelBuilder}, nhờ đó việc cân bằng độ khó (khoảng cách hố,
 * độ cao bậc thang, mật độ kẻ địch) rất dễ chỉnh sửa.</p>
 *
 * <p>Quy ước thiết kế:</p>
 * <ul>
 *   <li>Mặt đất chính nằm ở hàng {@value #GROUND_TOP}.</li>
 *   <li>Người chơi nhảy xa tối đa ~5 ô, lên cao tối đa ~3 ô.</li>
 *   <li>Mỗi màn có 6-9 Time Shard, cần 60% để mở cổng ra.</li>
 * </ul>
 */
public final class LevelFactory {

    /** Số màn chơi của trò chơi. */
    public static final int LEVEL_COUNT = 5;

    private static final int GROUND_TOP = 20;
    private static final int HEIGHT = 24;

    private LevelFactory() {
    }

    /** Tên màn chơi, dùng cho menu chọn màn. */
    public static String levelName(int index) {
        switch (index) {
            case 0:
                return "Thành Phố Vỡ Vụn";
            case 1:
                return "Khu Rừng Lãng Quên";
            case 2:
                return "Di Tích Cổ Đại";
            case 3:
                return "Tương Lai Băng Giá";
            case 4:
                return "Lõi Thời Gian";
            default:
                return "???";
        }
    }

    /**
     * Tạo màn chơi theo chỉ số.
     *
     * @param index 0..4
     */
    public static Level create(Game game, int index) {
        switch (index) {
            case 0:
                return brokenCity(game);
            case 1:
                return forgottenForest(game);
            case 2:
                return ancientRuins(game);
            case 3:
                return frozenFuture(game);
            case 4:
                return chronoCore(game);
            default:
                return brokenCity(game);
        }
    }

    // =======================================================================
    // Màn 1 - Thành Phố Vỡ Vụn (hướng dẫn)
    // =======================================================================
    private static Level brokenCity(Game game) {
        LevelBuilder b = new LevelBuilder(game, 0, levelName(0), "Chương I",
                "Alex tỉnh dậy giữa thành phố đổ nát. Chiếc đồng hồ trên tay "
                        + "vẫn còn chạy - và nó có thể bẻ cong thời gian.\n\n"
                        + "[A]/[D] di chuyển   [SPACE] nhảy   [SHIFT] lướt   [J] tấn công\n"
                        + "[E] đổi thời đại khi đứng trong Cổng Thời Gian   [Q] tua ngược   [ESC] tạm dừng",
                100, HEIGHT);

        b.ground(0, 99, GROUND_TOP);
        b.player(3, GROUND_TOP - 1);

        // --- 1. Hố nhỏ đầu tiên: dạy cách nhảy ---
        b.pit(11, 3);
        b.shard(12, 18);
        b.coin(6, 19);
        b.coin(8, 19);

        // --- 2. Cổng thời gian + cây cầu đứt ở HIỆN TẠI ---
        b.gate(17, GROUND_TOP - 1);
        b.pit(22, 6);
        b.bridge(22, GROUND_TOP, 6);
        b.shard(24, 18);
        b.coins(23, 18, 4);

        // --- 3. Kẻ địch đầu tiên ---
        b.slime(31, GROUND_TOP - 1);
        b.coin(33, 19);
        b.coin(34, 19);

        // --- 4. Bãi gai ---
        b.spikes(36, GROUND_TOP, 3);
        b.coinArc(36, 17, 3);

        // --- 5. Bệ di chuyển qua vực ---
        b.pit(45, 5);
        b.movingPlatformH(45, 18, 3, 4.0, 1.3);
        b.shard(47, 17);

        // --- 6. Cao nguyên: leo lên lấy shard ---
        b.stairs(52, 19, 4, true);
        b.ground(56, 63, 16);
        b.coins(56, 15, 6);
        b.shard(60, 14);
        b.slime(59, 15);
        b.stairs(64, 16, 4, false);
        b.checkpoint(50, 19);

        // --- 7. Đoạn kết ---
        b.bat(72, 15);
        b.crystal(70, 19);
        b.spikes(76, GROUND_TOP, 2);
        b.shard(80, 19);
        b.coins(86, 17, 5);
        b.shard(88, 17);
        b.health(84, 19);

        b.exit(94, GROUND_TOP - 1);
        return b.build();
    }

    // =======================================================================
    // Màn 2 - Khu Rừng Lãng Quên (Chrono Bat)
    // =======================================================================
    private static Level forgottenForest(Game game) {
        LevelBuilder b = new LevelBuilder(game, 1, levelName(1), "Chương II",
                "Khu rừng già nuốt chửng những kẻ lạc bước. Lũ Dơi Thời Gian "
                        + "treo mình trên tán cây, chỉ trực lao xuống.\n\n"
                        + "Mẹo: dơi chỉ lao tới khi thấy bạn. Cắt đuôi chúng bằng [SHIFT].",
                110, HEIGHT);

        b.ground(0, 109, GROUND_TOP);
        b.player(3, GROUND_TOP - 1);
        b.shard(8, 18);
        b.crystal(11, GROUND_TOP - 1);

        // --- Cầu đứt ở HIỆN TẠI ---
        b.gate(15, GROUND_TOP - 1);
        b.pit(20, 5);
        b.bridge(20, GROUND_TOP, 5);
        b.shard(22, 18);
        b.coins(21, 18, 3);

        // --- Tán cây: dơi và bệ nhảy ---
        b.bat(28, 14);
        b.bat(32, 15);
        b.slime(30, GROUND_TOP - 1);
        b.bat(37, 12);
        b.pit(34, 4);
        b.movingPlatformH(35, 18, 3, 3.5, 1.1);
        b.shard(36, 17);

        // --- Bãi gai + bệ rơi ---
        b.spikes(42, GROUND_TOP, 3);
        b.pit(48, 6);
        b.fallingPlatform(48, 17, 3);
        b.fallingPlatform(51, 15, 3);
        b.shard(52, 13);
        b.coins(49, 16, 3);
        b.checkpoint(46, 19);

        // --- Rừng sâu: nhiều dơi ---
        b.bat(60, 13);
        b.bat(64, 16);
        b.bat(68, 11);
        b.slime(62, GROUND_TOP - 1);
        b.slime(70, GROUND_TOP - 1);
        b.coinArc(58, 17, 5);
        b.health(66, GROUND_TOP - 1);

        // --- Cổng thời gian thứ hai: bệ QUÁ KHỨ ---
        b.gate(74, GROUND_TOP - 1);
        b.pit(76, 9);
        b.pastPlatform(77, 18, 3);
        b.pastPlatform(82, 16, 3);
        b.shard(83, 15);
        b.coins(78, 17, 3);

        // --- Về đích ---
        b.bat(88, 12);
        b.crystal(92, GROUND_TOP - 1);
        b.spikes(94, GROUND_TOP, 3);
        b.shard(98, 18);
        b.coins(100, 18, 4);
        b.health(102, GROUND_TOP - 1);

        b.exit(105, GROUND_TOP - 1);
        return b.build();
    }

    // =======================================================================
    // Màn 3 - Di Tích Cổ Đại (puzzle thời gian + Time Guardian)
    // =======================================================================
    private static Level ancientRuins(Game game) {
        LevelBuilder b = new LevelBuilder(game, 2, levelName(2), "Chương III",
                "Những phiến đá khắc ký tự thời gian. Nơi đây các thời đại "
                        + "chồng lên nhau - và Thời Vệ Binh canh giữ chúng.\n\n"
                        + "Mẹo: bệ chỉ tồn tại ở một thời đại. Hãy đổi thời đại đúng lúc.",
                120, HEIGHT);

        b.ground(0, 119, GROUND_TOP);
        b.player(3, GROUND_TOP - 1);
        b.shard(7, 18);

        // --- Puzzle 1: cầu đứt ở HIỆN TẠI ---
        b.gate(12, GROUND_TOP - 1);
        b.pit(18, 6);
        b.bridge(18, GROUND_TOP, 6);
        b.shard(20, 18);
        b.coins(19, 18, 4);

        // --- Puzzle 2: bệ chỉ có ở QUÁ KHỨ ---
        b.gate(24, GROUND_TOP - 1);
        b.pit(28, 7);
        b.pastPlatform(28, 18, 3);
        b.pastPlatform(32, 16, 3);
        b.shard(33, 15);
        b.coins(29, 17, 3);

        // --- Vệ binh canh cổng ---
        b.gate(40, GROUND_TOP - 1);
        b.guardian(45, GROUND_TOP - 1);
        b.shard(47, 18);
        b.health(43, GROUND_TOP - 1);

        // --- Puzzle 3: bệ TƯƠNG LAI trên hồ dung nham ---
        b.gate(50, GROUND_TOP - 1);
        b.clear(52, GROUND_TOP, 10, 1);
        b.lava(52, GROUND_TOP + 1, 10);
        b.futurePlatform(53, 18, 3);
        b.futurePlatform(58, 18, 3);
        b.shard(56, 17);
        b.coins(54, 17, 3);

        // --- Bẫy nghiền + vệ binh ---
        b.crusher(70, 15, 2, 5.0);
        b.shard(72, 18);
        b.guardian(78, GROUND_TOP - 1);
        b.crystal(76, GROUND_TOP - 1);
        b.spikes(66, GROUND_TOP, 4);
        b.checkpoint(64, 19);

        // --- Puzzle 4: bệ thời gian chỉ tồn tại ở QUÁ KHỨ / TƯƠNG LAI ---
        b.gate(82, GROUND_TOP - 1);
        b.pit(84, 8);
        b.temporalPlatform(85, 18, 3, 1, TimeState.MASK_PAST | TimeState.MASK_FUTURE);
        b.temporalPlatform(89, 16, 3, 1, TimeState.MASK_PAST | TimeState.MASK_FUTURE);
        b.shard(87, 15);
        b.coins(90, 15, 3);

        // --- Về đích: phải quay lại HIỆN TẠI mới qua được ---
        b.bat(96, 14);
        b.health(98, GROUND_TOP - 1);
        b.gate(100, GROUND_TOP - 1);
        b.shard(101, 18);
        b.coins(103, 18, 5);
        b.pit(106, 4);
        b.temporalPlatform(106, GROUND_TOP, 4, 1, TimeState.MASK_PRESENT);
        b.shard(107, 17);

        b.exit(114, GROUND_TOP - 1);
        return b.build();
    }

    // =======================================================================
    // Màn 4 - Tương Lai Băng Giá (Time Freeze + Temporal Knight)
    // =======================================================================
    private static Level frozenFuture(Game game) {
        LevelBuilder b = new LevelBuilder(game, 3, levelName(3), "Chương IV",
                "Thế giới đã chết trong băng. Kẻ địch ở đây không chỉ đánh trả - "
                        + "hắn tua ngược chính vết thương của mình.\n\n"
                        + "[F] Đóng Băng Thời Gian để khoá chân kẻ địch trong vài giây.",
                120, HEIGHT);

        b.ground(0, 119, GROUND_TOP);
        b.player(3, GROUND_TOP - 1);
        b.shard(7, 18);

        // --- Băng trơn ---
        b.ice(10, GROUND_TOP, 8);
        b.slime(13, GROUND_TOP - 1);
        b.shard(16, 18);
        b.coin(11, 19);
        b.coin(15, 19);

        // --- Cầu đứt ---
        b.gate(21, GROUND_TOP - 1);
        b.pit(25, 5);
        b.bridge(25, GROUND_TOP, 5);
        b.shard(27, 18);
        b.coins(26, 18, 3);

        // --- Hành lang dơi: nên dùng Đóng Băng ---
        b.bat(33, 13);
        b.bat(37, 15);
        b.bat(41, 12);

        b.ice(44, GROUND_TOP, 10);
        b.slime(48, GROUND_TOP - 1);
        b.spikes(56, GROUND_TOP, 3);

        b.pit(60, 4);
        b.movingPlatformH(60, 18, 3, 3.5, 1.1);
        b.shard(62, 17);
        b.crystal(68, GROUND_TOP - 1);

        // --- Hành lang dơi dày đặc (bài học Đóng Băng Thời Gian) ---
        b.bat(72, 14);
        b.bat(75, 16);
        b.bat(78, 13);
        b.bat(81, 15);
        b.health(73, GROUND_TOP - 1);
        b.shard(76, 19);
        b.checkpoint(70, 19);

        // --- Đấu trường Hiệp Sĩ Thời Gian ---
        b.guardian(88, GROUND_TOP - 1);
        b.knight(94, GROUND_TOP - 1);
        b.shard(96, 18);
        b.health(90, GROUND_TOP - 1);

        // --- Về đích ---
        b.ice(100, GROUND_TOP, 8);
        b.coins(102, 18, 5);
        b.shard(104, 18);
        b.gate(104, GROUND_TOP - 1);
        b.pit(108, 3);
        b.bridge(108, GROUND_TOP, 3);

        b.exit(114, GROUND_TOP - 1);
        return b.build();
    }

    // =======================================================================
    // Màn 5 - Lõi Thời Gian (trùm cuối: The Paradox)
    // =======================================================================
    private static Level chronoCore(Game game) {
        LevelBuilder b = new LevelBuilder(game, 4, levelName(4), "Chương V",
                "Lõi Thời Gian - nơi mọi dòng chảy hội tụ. Và kẻ đứng canh nó "
                        + "chính là Alex... của một tương lai đã mất hết nhân tính.\n\n"
                        + "Hạ gục THE PARADOX để kết thúc vòng lặp.",
                130, HEIGHT);

        b.ground(0, 129, GROUND_TOP);
        b.player(3, GROUND_TOP - 1);
        b.shard(9, 18);
        b.slime(12, GROUND_TOP - 1);

        // --- Cầu đứt ---
        b.gate(16, GROUND_TOP - 1);
        b.pit(20, 6);
        b.bridge(20, GROUND_TOP, 6);
        b.shard(22, 18);
        b.coins(21, 18, 4);

        b.bat(28, 14);
        b.slime(32, GROUND_TOP - 1);
        b.spikes(36, GROUND_TOP, 3);
        b.coinArc(36, 17, 3);

        // --- Puzzle QUÁ KHỨ ---
        b.gate(38, GROUND_TOP - 1);
        b.pit(40, 10);
        b.pastPlatform(41, 18, 3);
        b.pastPlatform(46, 16, 3);
        b.shard(42, 17);
        b.shard(47, 15);
        b.coins(43, 17, 3);

        // --- Đoạn giữa: vệ binh và dơi ---
        b.guardian(54, GROUND_TOP - 1);
        b.slime(58, GROUND_TOP - 1);
        b.bat(62, 13);
        b.crystal(56, GROUND_TOP - 1);
        b.checkpoint(51, 19);

        // --- Puzzle TƯƠNG LAI ---
        b.gate(64, GROUND_TOP - 1);
        b.pit(66, 8);
        b.futurePlatform(67, 18, 3);
        b.futurePlatform(72, 16, 3);
        b.shard(68, 17);
        b.coins(73, 15, 3);
        b.bat(70, 12);

        // --- Hành lang cuối ---
        b.spikes(80, GROUND_TOP, 4);
        b.health(86, GROUND_TOP - 1);
        b.slime(84, GROUND_TOP - 1);
        b.bat(88, 14);
        b.guardian(92, GROUND_TOP - 1);

        // --- Đấu trường trùm ---
        b.shard(97, 18);
        b.coins(95, 18, 6);
        b.crystal(100, GROUND_TOP - 1);
        b.health(104, GROUND_TOP - 1);
        b.checkpoint(102, 19);

        TheParadox paradox = new TheParadox(120 * Physics.TILE_SIZE,
                GROUND_TOP * Physics.TILE_SIZE - 64.0);
        paradox.setArena(112 * Physics.TILE_SIZE, 130 * Physics.TILE_SIZE);
        b.level().spawn(paradox);

        b.exit(127, GROUND_TOP - 1);
        return b.build();
    }
}
