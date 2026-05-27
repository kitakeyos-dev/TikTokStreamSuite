# TikTokStreamSuite

**Tiếng Việt** | [English](README.md)

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg?style=flat-square)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg?style=flat-square)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

Công cụ hỗ trợ streamer TikTok Live viết bằng Java 17 + JavaFX 21. Giao diện dark theme (AtlantaFX Dracula).

---

## Tính năng

### 🔊 Đọc bình luận (TTS)
- Dùng Google Translate TTS để đọc bình luận chat, xử lý trực tiếp trong RAM, không ghi file tạm.
- Chọn thiết bị âm thanh đầu ra (để tách riêng khỏi luồng stream nếu cần).
- Tự động tăng tốc độ đọc khi chat đông (backlog > 2 → 1.25x, > 4 → 1.5x).
- Lọc từ cấm, tùy chọn đọc tên người bình luận.

### 📊 Bảng xếp hạng & thống kê
- Tab tổng quan: số người xem, lượt thích, quà tặng, trạng thái kết nối.
- Leaderboard: top tặng quà, top thả tim, cập nhật realtime.
- Tab likes: theo dõi mốc thả tim.
- Tab team: theo dõi tiến trình thi đấu đội nhóm.
- Tab chat: xem toàn bộ bình luận, có lọc.

### ⚙️ Tự động hóa theo sự kiện
- Định nghĩa quy tắc: trigger (tặng quà / thả tim / follow / bình luận) → hành động (phát âm, overlay, lệnh).
- Quản lý quy tắc trong tab Actions & Events.

### 🎬 Overlay
- Hiển thị cửa sổ overlay thông báo sự kiện trực tiếp trên màn hình.
- Quản lý qua tab Overlays.

### 🔄 Tự cập nhật
- Tự kiểm tra phiên bản mới từ GitHub, tải về, rồi tắt app và chạy installer tự động.

---

## Yêu cầu

- Windows 10/11 (64-bit)
- JDK 17+ (nếu chạy từ source)
- Kết nối Internet

---

## Chạy từ source

```bash
git clone https://github.com/kitakeyos-dev/TikTokStreamSuite.git
cd TikTokStreamSuite

# Biên dịch
mvn clean compile

# Chạy
mvn javafx:run

# Đóng gói thành .exe (Windows)
build-exe.bat
```

Sau khi chạy `build-exe.bat`, thư mục `dist\TikTokStreamSuite\` chứa file exe không cần cài Java.

---

## Cấu trúc dự án

```text
TikTokStreamSuite/
├── pom.xml
├── build-exe.bat
├── update.json
└── src/main/java/com/leaderboard/
    ├── App.java
    ├── model/
    │   ├── Gifter.java
    │   ├── Liker.java
    │   ├── TeamMember.java
    │   └── action/
    │       ├── StreamRule.java
    │       ├── TriggerType.java
    │       └── ActionType.java
    ├── service/
    │   ├── ServiceLocator.java
    │   ├── StreamSessionMediator.java
    │   ├── ITTSService.java / TTSService.java
    │   ├── ITikTokConnector.java / TikTokConnector.java
    │   ├── IActionRulesEngine.java / ActionRulesEngine.java
    │   ├── UpdateService.java
    │   └── impl/
    │       ├── TTSServiceImpl.java
    │       ├── TikTokConnectorImpl.java
    │       └── ActionRulesEngineImpl.java
    ├── ui/
    │   ├── DashboardStage.java
    │   ├── DashboardLayout.java
    │   ├── Dialogs.java
    │   ├── SplashScreen.java
    │   ├── ToggleSwitch.java
    │   ├── overlay/
    │   └── tab/
    │       ├── BaseDataTab.java
    │       ├── OverviewTab.java
    │       ├── LeaderboardTab.java
    │       ├── LikesTab.java
    │       ├── TeamTab.java
    │       ├── ChatTab.java
    │       ├── ActionsEventsTab.java
    │       ├── OverlaysTab.java
    │       └── TtsTab.java
    └── util/
        ├── ConfigManager.java
        ├── DataManager.java
        ├── EmojiParser.java
        ├── I18n.java
        ├── IconManager.java
        └── ResizeHelper.java
```

---

## Kiến trúc

- **ServiceLocator** — registry trung tâm, inject implementation theo interface.
- **StreamSessionMediator** — nhận sự kiện TikTok rồi phân phối tới các tab, không để UI tự kết nối trực tiếp.
- **Facade** (`TTSService`, `TikTokConnector`, `ActionRulesEngine`) — wrapper tĩnh để code cũ gọi được mà không cần biết ServiceLocator.
- **BaseDataTab\<T\>** — base class chung cho các tab hiển thị bảng dữ liệu.

---

## Cập nhật tự động (`update.json`)

```json
{
  "version": "1.0.1",
  "downloadUrl": "https://github.com/kitakeyos-dev/TikTokStreamSuite/releases/download/v1.0.1/TikTokStreamSuite_Setup.exe",
  "changelog": "...",
  "forceUpdate": false
}
```

---

## License

MIT — **Hoàng Hữu Dũng (kitakeyos)**
