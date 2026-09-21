# ⏳ Chrono Runner

> **Chrono Runner** là tựa game 2D Platformer phiêu lưu hành động kết hợp cơ chế **thao túng thời gian**, được xây dựng hoàn toàn bằng **Java thuần (Java2D / Swing)** mà không cần bất kỳ game engine hay thư viện bên thứ ba nào.

---

## 📖 Cốt truyện

Nhân vật chính **Alex** tỉnh dậy giữa một thành phố đổ nát trong tương lai vỡ vụn. Manh mối duy nhất còn lại là một chiếc đồng hồ đặc biệt trên tay — một thiết bị cổ xưa có khả năng bẻ cong không gian và thời gian. Alex phải du hành qua ba dòng thời gian (**Quá khứ**, **Hiện tại**, **Tương lai**) để thu thập các Mảnh Thời Gian (*Time Shards*), giải mã những nghịch lý thời gian và đánh bại thực thể tà ác **The Paradox** tại Lõi Thời Gian để khôi phục lại thực tại.

---

## 🎮 Cơ chế chơi chính

### 1. Thao túng thời gian (Time Manipulation)
- **Dịch chuyển thời đại (Time Shift):** Kích hoạt tại các **Cổng Thời Gian (Time Gate)** để chuyển đổi giữa **Quá khứ - Hiện tại - Tương lai**. Mỗi thời đại sẽ thay đổi môi trường, xuất hiện hoặc biến mất các nền tảng thời gian (*Temporal Platforms*), cành cây cổ thụ, hoặc các khối kiến trúc.
- **Tua ngược thời gian (Rewind - `Q`):** Ghi nhận lại vị trí và trạng thái trong vài giây trước đó, cho phép Alex tua ngược lại khi lỡ bước sẩy chân xuống hố hoặc trúng đòn bẫy.
- **Đóng băng thời gian (Time Freeze - `F`):** Tạm thời làm ngưng đọng chuyển động của kẻ địch, đạn đạo và các bẫy cơ học xung quanh.

### 2. Hành động & Di chuyển
- Chạy bộ, nhảy đơn và **nhảy đôi (Double Jump)**.
- **Lướt nhanh (Dash - `Shift`):** Lao về phía trước trong chớp mắt, xuyên qua các khe hẹp hoặc né tránh đòn đánh.
- **Tấn công cận chiến (Slash - `J`):** Tung nhát chém bằng thanh kiếm năng lượng để tiêu diệt kẻ địch và kích hoạt cơ quan.

### 3. Thu thập & Nâng cấp (Upgrade System)
- **Chrono Coin:** Tiền tệ dùng để mua và nâng cấp kỹ năng tại trạm nâng cấp (*Upgrade Screen*).
- **Time Shards:** Mảnh vỡ thời gian rải rác khắp màn chơi; cần thu thập đủ số lượng yêu cầu (tối thiểu ~60%) để kích hoạt **Cổng Thoát (Exit Portal)**.
- **Cây kỹ năng nâng cấp:**
  - *Tua Ngược (Rewind Duration):* Tăng thời lượng có thể tua lại.
  - *Năng Lượng (Time Energy):* Tăng mức năng lượng tối đa để dùng chiêu.
  - *Lướt (Dash):* Mở khoá và giảm thời gian hồi lướt.
  - *Nhảy Đôi (Double Jump):* Cho phép nhảy thêm lần 2 trên không trung.
  - *Đóng Băng (Time Freeze):* Mở khoá khả năng làm chậm/đóng băng kẻ địch.

---

## 🗺️ Hệ thống màn chơi (5 Chương)

| Chương | Tên Màn | Đặc điểm môi trường & Thử thách |
| :---: | :--- | :--- |
| **I** | **Thành Phố Vỡ Vụn** *(Broken City)* | Màn mở đầu hướng dẫn làm quen với nhảy hố, cầu gãy và sử dụng Cổng Thời Gian. |
| **II** | **Khu Rừng Lãng Quên** *(Forgotten Forest)* | Cây cối biến đổi giữa thời đại mầm non và đại thụ; xuất hiện dơi thời gian (*Chrono Bat*). |
| **III** | **Di Tích Cổ Đại** *(Ancient Ruins)* | Đền đài với bẫy nghiền (*Crusher Trap*), chông nhọn và lính canh cổ xưa (*Time Guardian*). |
| **IV** | **Tương Lai Băng Giá** *(Frozen Future)* | Nền trơn trượt, nền rơi vỡ (*Falling Platform*), cạm bẫy dày đặc đòi hỏi phối hợp kỹ năng lướt và đóng băng. |
| **V** | **Lõi Thời Gian** *(Chrono Core)* | Màn đấu Boss hoành tráng chống lại **The Paradox** cùng các phân thân và kỵ sĩ thời gian (*Temporal Knight*). |

---

## ⌨️ Bảng điều khiển (Controls)

| Thao tác | Phím chính | Phím thay thế |
| :--- | :---: | :---: |
| **Di chuyển trái / phải** | `A` / `D` | `←` / `→` |
| **Nhảy** | `SPACE` | `W` / `↑` |
| **Lướt (Dash)** | `SHIFT` | — |
| **Tấn công (Attack)** | `J` | — |
| **Tương tác / Đổi thời đại** | `E` | — |
| **Tua ngược (Rewind)** | `Q` | — |
| **Đóng băng (Freeze)** | `F` | — |
| **Tạm dừng / Quay lại** | `ESC` | — |
| **Xác nhận / Bắt đầu** | `ENTER` | Nhấp chuột trái |
| **Chơi lại màn** | `R` | — |

---

## 📂 Cấu trúc dự án

```text
chrono-runner/
├── build.bat                   # Script biên dịch mã nguồn Java
├── run.bat                     # Script chạy game (tự động build nếu chưa có bản compile)
├── README.md                   # Tài liệu hướng dẫn dự án
├── saves/                      # Thư mục lưu tiến trình chơi (profile game)
└── src/
    └── chronorunner/
        ├── Main.java           # Điểm khởi động game (Swing EDT entrypoint)
        ├── core/               # Lõi hệ thống: Game loop, Camera, Physics, TimeManager, Input
        ├── entity/             # Thực thể: Player, GameObject, Character, Projectile, Particle
        │   ├── enemy/          # Kẻ địch: TimeSlime, ChronoBat, TimeGuardian,...
        │   └── boss/           # Trùm cuối: TheParadox, ParadoxClone, TemporalKnight,...
        ├── item/               # Vật phẩm: ChronoCoin, TimeShard, HealthOrb, TimeCrystal
        ├── obstacle/           # Chướng ngại vật: Platforms, Bẫy đè, TimeGate, ExitPortal
        ├── ui/                 # Giao diện người dùng: HUD, Menu, LevelSelect, Upgrade, Store
        ├── util/               # Tiện ích: SaveManager, Collision AABB, Upgrade, Progress
        └── world/              # Quản lý thế giới: Level, LevelBuilder, LevelFactory, TileType
```

---

## ⚙️ Yêu cầu hệ thống

- **Hệ điều hành:** Windows, macOS, hoặc Linux.
- **Java:** **JDK 17 trở lên** (khuyến nghị JDK 21+).
- **Phần cứng:** Cấu hình cơ bản (màn hình hỗ trợ hiển thị tỷ lệ 16:9, độ phân giải nội bộ chuẩn `1280 x 720`).

---

## 🚀 Hướng dẫn cài đặt & Chạy game

### Cách 1: Sử dụng Script có sẵn (Windows)

1. **Biên dịch game:**
   Nhấp đúp hoặc chạy script `build.bat` trong command line:
   ```cmd
   build.bat
   ```
2. **Khởi chạy trò chơi:**
   Chạy script `run.bat`:
   ```cmd
   run.bat
   ```

### Cách 2: Biên dịch thủ công qua Terminal / Command Prompt

Nếu bạn dùng Linux / macOS hoặc muốn biên dịch bằng dòng lệnh:

```bash
# 1. Tạo thư mục chứa file class biên dịch
mkdir -p out

# 2. Tìm tất cả các file .java và biên dịch
javac -encoding UTF-8 -d out $(find src -name "*.java")

# 3. Chạy game
java -cp out chronorunner.Main
```

---

## 🛠️ Điểm nổi bật về mặt kỹ thuật

- **Pure Java & Java2D:** Sử dụng `Graphics2D` với kỹ thuật Double Buffering, Anti-aliasing, ánh xạ camera và bộ đệm cố định `1280x720` tự co giãn thích ứng với màn hình.
- **Fixed Time-Step Game Loop:** Chu kỳ cập nhật vật lý ổn định `60 FPS`, đảm bảo chuyển động mượt mà và không bị phụ thuộc vào tốc độ khung hình hiển thị.
- **Mô hình kiến trúc rõ ràng:**
  - **Screen State Stack:** Quản lý chuyển cảnh mượt mà giữa Menu chính, Chọn màn, Trận đấu và Màn tạm dừng (*Pause Screen*).
  - **Observer Pattern:** `TimeManager` phát tín hiệu thay đổi dòng thời gian tới tất cả các thực thể đang hoạt động trong thế giới.
  - **Builder Pattern:** `LevelBuilder` giúp mô tả các màn chơi một cách trực quan, dễ mở rộng và tinh chỉnh chỉ số.