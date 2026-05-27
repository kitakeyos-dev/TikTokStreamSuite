# 🎥 TikTokStreamSuite (TikTok Live Stream Suite)

**Tiếng Việt** | [English](README.md)

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg?style=flat-square)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg?style=flat-square)](https://openjfx.io/)
[![Dracula Theme](https://img.shields.io/badge/Theme-AtlantaFX--Dracula-purple.svg?style=flat-square)](https://github.com/mkpaz/atlantafx)
[![Build Status](https://img.shields.io/badge/Platform-Windows%20x64-blueviolet.svg?style=flat-square)](https://github.com/kitakeyos-dev/TikTokStreamSuite)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

**TikTokStreamSuite** là giải pháp phần mềm chuyên nghiệp, toàn diện và trực quan được thiết kế đặc biệt dành cho các nhà sáng tạo nội dung và streamer trên nền tảng **TikTok Live**. Ứng dụng được xây dựng trên **Java 17 & JavaFX 21**, mang giao diện tối Dracula hiện đại (AtlantaFX Dracula) cùng phong cách Bento Grid sang trọng và trực quan. Phần mềm giúp streamer tối ưu tương tác phòng live, quản lý bảng xếp hạng, tự động hóa hành động theo sự kiện và xử lý chat thời gian thực — tất cả trong một công cụ duy nhất, không cần cài đặt thêm phần mềm cồng kềnh.

---

## ✨ Tính Năng Nổi Bật

### 🔊 1. Trình Đọc Bình Luận AI (Smart Vietnamese TTS)
*   **Công nghệ In-Memory:** Giải mã trực tiếp luồng âm thanh MP3 từ Google Translate API sang PCM trong RAM thông qua thư viện `JLayer`, tuyệt đối không tạo file tạm — tăng tốc độ phản hồi và bảo vệ ổ cứng.
*   **Định tuyến Mixer linh hoạt:** Cho phép streamer chọn chính xác cổng ra âm thanh (tai nghe riêng, card âm thanh phụ) để âm thanh TTS không bị phát ra trực tiếp trên luồng live nếu không mong muốn.
*   **Tự động tăng tốc độ đọc (Dynamic Rate Scaling):** Tự động phát hiện tần suất bình luận cao (backlog > 2 hoặc > 4 tin nhắn) và tăng tốc đọc từ `1.0x` lên `1.25x` hoặc `1.5x` bằng cách điều chỉnh sample rate phát sóng linh hoạt.
*   **Bộ lọc từ ngữ nhạy cảm:** Tích hợp bộ lọc từ cấm / từ tục tĩu giúp streamer giữ môi trường live sạch và lành mạnh.
*   **Cá nhân hóa đọc tên:** Tùy chỉnh bật/tắt đọc biệt danh (nickname) của người xem trước khi đọc bình luận.

### 📊 2. Quản Lý Thống Kê & Bảng Xếp Hạng Thời Gian Thực
*   **Overview Tab:** Dashboard dạng Bento Grid hiển thị các chỉ số cốt lõi — số người xem, lượt thích, tổng quà tặng, trạng thái kết nối.
*   **Leaderboard Tab:** Bảng xếp hạng trực tiếp vinh danh top tặng quà và top thả tim, cập nhật tự động từng giây.
*   **Likes Tab:** Theo dõi mốc lượt thích với thanh tiến trình thời gian thực hướng tới mục tiêu đặt trước.
*   **Team Tab:** Theo dõi tiến trình thi đấu tổ đội và đóng góp của từng thành viên.
*   **Chat Tab:** Luồng tin nhắn tập trung, hiển thị bình luận sắc nét với font hiện đại và hỗ trợ lọc nội dung.

### ⚙️ 3. Engine Tự Động Hóa Theo Sự Kiện (Action Rules Engine)
*   **Tự động hóa theo quy tắc:** Định nghĩa các trigger (tặng quà, thả tim, follow, bình luận) ánh xạ tới hành động như phát âm thanh, kích hoạt overlay hoặc thực thi lệnh tùy chỉnh.
*   **Actions & Events Tab:** Giao diện quản lý đầy đủ để tạo, chỉnh sửa và bật/tắt các quy tắc live với bảng dữ liệu trực quan.
*   **Engine tách biệt:** `ActionRulesEngine` hoạt động qua `ServiceLocator`, hoàn toàn tách biệt khỏi logic giao diện để dễ kiểm thử và bảo trì.

### 🎬 4. Hệ Thống Overlay Trực Tiếp
*   **Overlay Manager:** Quản lý và hiển thị các cửa sổ overlay cho cảnh báo và thông báo sự kiện thời gian thực trực tiếp trên màn hình stream.
*   **Overlays Tab:** Điều chỉnh hiển thị, vị trí và nội dung overlay trực tiếp từ dashboard mà không gián đoạn buổi live.

### 🚀 5. Hệ Thống Tự Động Cập Nhật Ẩn (Silent Auto-Updater)
*   **Asynchronous Checker:** Tự động kiểm tra phiên bản mới từ GitHub Metadata thông qua giao thức bảo mật API mà không gây đứng hình ứng dụng.
*   **Indigo Premium Progress UI:** Giao diện tiến trình Dracula-Indigo trong suốt hiển thị phần trăm tải xuống thời gian thực.
*   **Zero-Lock Installer Trigger:** Sau khi tải xong file `.exe`, ứng dụng tự động đóng tiến trình Java chính để giải phóng file lock và kích hoạt trình cài đặt — quá trình nâng cấp hoàn toàn tự động.

---

## 🛠️ Yêu Cầu Hệ Thống & Cài Đặt

*   **Hệ điều hành:** Windows 10/11 (64-bit).
*   **Java Runtime:** JDK 17 hoặc mới hơn (nếu chạy từ mã nguồn).
*   **Kết nối Internet:** Yêu cầu để kết nối TikTok Live API và Google Translate TTS API.

---

## 💻 Hướng Dẫn Biên Dịch & Chạy Mã Nguồn

Dự án được quản lý hoàn toàn bằng **Maven**.

### Bước 1: Clone dự án về máy tính
```bash
git clone https://github.com/kitakeyos-dev/TikTokStreamSuite.git
cd TikTokStreamSuite
```

### Bước 2: Biên dịch ứng dụng
```bash
mvn clean compile
```

### Bước 3: Khởi chạy ở chế độ Phát triển
```bash
mvn javafx:run
```

### Bước 4: Đóng gói thành phần mềm cài đặt (`.exe`) cho Windows
Dự án tích hợp sẵn script `build-exe.bat` sử dụng **jlink** và **jpackage** để đóng gói JRE thu gọn cùng JavaFX:
```cmd
build-exe.bat
```
*Sau khi hoàn tất, thư mục `dist\TikTokStreamSuite\` chứa phần mềm chạy trực tiếp mà không yêu cầu cài đặt Java trên máy khách.*

---

## 📂 Cấu Trúc Dự Án

```text
TikTokStreamSuite/
├── .github/                    # Cấu hình GitHub Actions / workflows
├── pom.xml                     # Maven (Retrofit, JLayer, AtlantaFX, OkHttp...)
├── build-exe.bat               # Script đóng gói Windows (jlink + jpackage)
├── update.json                 # Metadata cập nhật tự động từ xa
├── src/main/
│   ├── java/com/leaderboard/
│   │   ├── App.java                        # Điểm khởi chạy chính & Splash Screen
│   │   ├── model/
│   │   │   ├── Gifter.java                 # Model dữ liệu sự kiện tặng quà
│   │   │   ├── Liker.java                  # Model dữ liệu sự kiện thả tim
│   │   │   ├── TeamMember.java             # Model dữ liệu thành viên đội
│   │   │   └── action/
│   │   │       ├── StreamRule.java         # Thực thể quy tắc tự động (trigger → action)
│   │   │       ├── TriggerType.java        # Enum: gift, like, follow, comment
│   │   │       └── ActionType.java         # Enum: play audio, overlay, command...
│   │   ├── service/
│   │   │   ├── ServiceLocator.java         # IoC / registry dịch vụ trung tâm
│   │   │   ├── StreamSessionMediator.java  # Mediator: tách UI khỏi sự kiện TikTok
│   │   │   ├── ITTSService.java            # Interface dịch vụ TTS
│   │   │   ├── TTSService.java             # Facade tĩnh cho TTS
│   │   │   ├── ITikTokConnector.java       # Interface connector TikTok
│   │   │   ├── TikTokConnector.java        # Facade tĩnh cho TikTok connector
│   │   │   ├── IActionRulesEngine.java     # Interface engine quy tắc
│   │   │   ├── ActionRulesEngine.java      # Facade tĩnh cho rules engine
│   │   │   ├── UpdateService.java          # Kiểm tra & tải cập nhật bất đồng bộ
│   │   │   └── impl/
│   │   │       ├── TTSServiceImpl.java          # Triển khai TTS (giải mã & phát âm thanh)
│   │   │       ├── TikTokConnectorImpl.java     # Triển khai WebSocket TikTok Live
│   │   │       └── ActionRulesEngineImpl.java   # Khớp quy tắc & điều phối hành động
│   │   ├── ui/
│   │   │   ├── DashboardStage.java         # Cửa sổ chính & khởi tạo dịch vụ
│   │   │   ├── DashboardLayout.java        # Bố cục Bento-style tổng thể
│   │   │   ├── Dialogs.java                # Tiện ích dialog & cảnh báo Dracula theme
│   │   │   ├── SplashScreen.java           # Màn hình giật splash có animation
│   │   │   ├── ToggleSwitch.java           # Control Toggle Switch tùy chỉnh
│   │   │   ├── overlay/                    # Các thành phần cửa sổ overlay
│   │   │   └── tab/
│   │   │       ├── BaseDataTab.java        # Lớp cơ sở chung cho các tab dữ liệu
│   │   │       ├── OverviewTab.java        # Tab tổng quan chỉ số live
│   │   │       ├── LeaderboardTab.java     # Tab bảng xếp hạng thời gian thực
│   │   │       ├── LikesTab.java           # Tab theo dõi mốc lượt thích
│   │   │       ├── TeamTab.java            # Tab theo dõi thi đấu tổ đội
│   │   │       ├── ChatTab.java            # Tab luồng tin nhắn chat
│   │   │       ├── ActionsEventsTab.java   # Tab quản lý quy tắc tự động hóa
│   │   │       ├── OverlaysTab.java        # Tab quản lý overlay
│   │   │       └── TtsTab.java             # Tab cấu hình TTS nâng cao
│   │   └── util/
│   │       ├── ConfigManager.java          # Lưu trữ cài đặt ứng dụng (JSON)
│   │       ├── DataManager.java            # Lưu trữ dữ liệu runtime trong bộ nhớ
│   │       ├── EmojiParser.java            # Chuyển đổi emoji sang văn bản
│   │       ├── I18n.java                   # Tiện ích đa ngôn ngữ
│   │       ├── IconManager.java            # Trình tải tài nguyên icon
│   │       └── ResizeHelper.java           # Xử lý thay đổi kích thước cửa sổ không viền
│   └── resources/
│       ├── css/                            # Stylesheet tùy chỉnh giao diện
│       └── icons/                          # Logo và icon ứng dụng
```

---

## 🏗️ Kiến Trúc Tổng Quan

Ứng dụng được xây dựng trên kiến trúc phân lớp rõ ràng, giảm thiểu sự phụ thuộc giữa các thành phần:

| Pattern | Class | Vai trò |
|---|---|---|
| **Service Locator** | `ServiceLocator` | Registry trung tâm — phân giải các implementation theo interface |
| **Mediator** | `StreamSessionMediator` | Điều phối sự kiện TikTok tới các tab UI mà không cần kết nối trực tiếp |
| **Facade** | `TTSService`, `TikTokConnector`, `ActionRulesEngine` | Wrapper tĩnh tiện lợi bao bọc `ServiceLocator` |
| **Interface Segregation** | `ITTSService`, `ITikTokConnector`, `IActionRulesEngine` | Hợp đồng dịch vụ tách biệt hoàn toàn khỏi implementation |
| **Template Method** | `BaseDataTab<T>` | Boilerplate chung cho các tab dữ liệu dạng bảng |

---

## ⚙️ Cấu Hình Cập Nhật (`update.json`)

Hệ thống cập nhật hoạt động dựa trên file metadata trực tuyến cấu trúc như sau:

```json
{
  "version": "1.0.1",
  "downloadUrl": "https://github.com/kitakeyos-dev/TikTokStreamSuite/releases/download/v1.0.1/TikTokStreamSuite_Setup.exe",
  "changelog": "- Cải tiến tốc độ đọc bình luận tiếng Việt.\n- Cập nhật bộ lọc từ cấm.\n- Thiết kế lại icon theo định dạng vuông 38x38.",
  "forceUpdate": false
}
```

---

## 📝 Bản Quyền & Giấy Phép

Dự án thuộc sở hữu của **Hoàng Hữu Dũng (kitakeyos)** và được phát hành theo **MIT License**. Bạn có thể tự do phát triển, sao chép và thương mại hóa theo các điều khoản trong giấy phép.

---

## 🤝 Hỗ Trợ Dự Án

Nếu bạn thấy bộ công cụ này hữu ích cho phòng live của mình, hãy tặng dự án một **⭐️ Star** trên GitHub để ủng hộ nhà phát triển nhé!

*Chúc các streamer có những buổi phát sóng bùng nổ tương tác! 🚀*
