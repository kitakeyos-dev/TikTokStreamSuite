# 🎥 TikTokStreamSuite (TikTok Live Stream Suite)

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg?style=flat-square)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg?style=flat-square)](https://openjfx.io/)
[![Dracula Theme](https://img.shields.io/badge/Theme-AtlantaFX--Dracula-purple.svg?style=flat-square)](https://github.com/mkpaz/atlantafx)
[![Build Status](https://img.shields.io/badge/Platform-Windows%20x64-blueviolet.svg?style=flat-square)](https://github.com/kitakeyos-dev/TikTokStreamSuite)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

**TikTokStreamSuite** là giải pháp phần mềm chuyên nghiệp, toàn diện và trực quan được thiết kế đặc biệt dành cho các nhà sáng tạo nội dung và streamer trên nền tảng **TikTok Live**. Ứng dụng được xây dựng trên nền tảng **Java 17 & JavaFX 21**, mang giao diện tối Dracula hiện đại (AtlantaFX Dracula) cùng phong cách Bento Grid sang trọng và trực quan, giúp streamer tối ưu hóa tương tác phòng live, quản lý bảng xếp hạng và trò chuyện thời gian thực mà không cần cài đặt thêm các công cụ cồng kềnh.

---

## ✨ Tính Năng Nổi Bật

### 🔊 1. Trình Đọc Bình Luận Trí Tuệ Nhân Tạo (Smart Vietnamese TTS)
*   **Công nghệ in-memory:** Giải mã trực tiếp luồng âm thanh MP3 từ Google Translate API sang PCM thông qua thư viện `JLayer` trong bộ nhớ RAM, tuyệt đối không tạo file ghi âm tạm thời để tăng tốc độ phản hồi và bảo vệ ổ cứng.
*   **Định tuyến Mixer linh hoạt:** Cho phép streamer chọn chính xác cổng ra âm thanh (audio output interface) như tai nghe riêng biệt hoặc card âm thanh phụ, tránh làm âm thanh phát ra trực tiếp trên luồng live nếu streamer không mong muốn.
*   **Tự động tăng tốc độ đọc (Dynamic Rate Scaling):** Tự động phát hiện khi phòng chat có tần suất bình luận cao (backlog > 2 hoặc > 4 tin nhắn) để tăng tốc độ đọc từ `1.0x` lên `1.25x` và `1.5x` bằng cách tái mẫu mẫu tần số phát sóng (sample rate scale) linh hoạt.
*   **Bộ lọc từ ngữ nhạy cảm:** Tích hợp bộ lọc từ cấm, từ tục tĩu (Blocked/Profanity Words Filter) giúp streamer giữ môi trường live sạch sẽ.
*   **Cá nhân hóa đọc tên:** Tùy chỉnh bật/tắt đọc biệt danh (nickname) của người dùng trước khi đọc bình luận.

### 📊 2. Quản Lý Thống Kê & Bảng Xếp Hạng Thời Gian Thực
*   **Overview Bento Tab:** Giao diện Bento grid thời thượng hiển thị các chỉ số cốt lõi (Số người xem hiện tại, lượt thích, tổng số quà tặng, trạng thái kết nối).
*   **Leaderboard Tab:** Bảng xếp hạng trực tiếp vinh danh những người ủng hộ (top tặng quà, top tương tác thả tim) cập nhật tự động trong từng giây.
*   **Team & Likes Tracker:** Theo dõi tiến trình thi đấu tổ đội (team) và định lượng chỉ số thả tim theo các mốc tương tác thời gian thực.
*   **Chat Tab:** Luồng tin nhắn tập trung, hiển thị bình luận sắc nét với font chữ hiện đại, hỗ trợ lọc và xem danh sách tương tác mượt mà.

### 🚀 3. Hệ Thống Tự Động Cập Nhật Hiện Đại (Silent Auto-Updater)
*   **Asynchronous Checker:** Tự động kết nối kiểm tra phiên bản mới từ máy chủ GitHub Metadata thông qua giao thức bảo mật API mà không gây đứng hình ứng dụng.
*   **Indigo Premium Progress UI:** Giao diện tiến trình Dracula-Indigo trong suốt độc đáo hiển thị phần trăm dung lượng tải xuống thời gian thực.
*   **Zero-Lock Installer Trigger:** Sau khi tải xong trình cài đặt `.exe`, ứng dụng tự động đóng luồng Java chính để mở khóa tệp tin hệ thống và kích hoạt chương trình cài đặt mới giúp quá trình nâng cấp diễn ra hoàn toàn tự động.

---

## 🛠️ Yêu Cầu Hệ Thống & Cài Đặt

*   **Hệ điều hành:** Windows 10/11 (64-bit).
*   **Bộ xử lý Java:** JDK 17 hoặc mới hơn (nếu chạy từ mã nguồn).
*   **Kết nối Internet:** Yêu cầu đường truyền mạng để kết nối TikTok Live API và Google Translate TTS API.

---

## 💻 Hướng Dẫn Biên Dịch & Chạy Mã Nguồn

Dự án được quản lý hoàn toàn bằng **Maven**. Bạn có thể dễ dàng tải về, build mã nguồn và đóng gói thành tệp tin chạy trực tiếp trên Windows theo hướng dẫn dưới đây.

### Bước 1: Clone dự án về máy tính
```bash
git clone https://github.com/kitakeyos-dev/TikTokStreamSuite.git
cd TikTokStreamSuite
```

### Bước 2: Biên dịch ứng dụng
Sử dụng Maven để tải các thư viện phụ thuộc và biên dịch ứng dụng JavaFX:
```bash
mvn clean compile
```

### Bước 3: Khởi chạy ở chế độ Phát triển (Development)
Chạy ứng dụng trực tiếp bằng JavaFX plugin:
```bash
mvn javafx:run
```

### Bước 4: Đóng gói thành phần mềm cài đặt (`.exe`) cho Windows
Ứng dụng tích hợp sẵn tập lệnh đóng gói chuyên nghiệp `build-exe.bat` sử dụng tính năng **jlink** và **jpackage** để đóng gói một JRE thu gọn đi kèm JavaFX. Bạn chỉ cần chạy lệnh sau:
```cmd
build-exe.bat
```
*Sau khi chạy thành công, thư mục `dist\TikTokStreamSuite\` chứa phần mềm chạy trực tiếp cực kỳ mượt mà mà không yêu cầu máy khách phải cài đặt sẵn Java.*

---

## 📂 Cấu Trúc Dự Án

```text
TikTokStreamSuite/
├── .github/                 # Cấu hình GitHub Actions / workflows
├── javafx-jmods.zip         # Gói JavaFX JMods dùng để đóng gói jpackage
├── pom.xml                  # Tệp cấu hình các dependency (Retrofit, JLayer, AtlantaFX, OkHttp...)
├── build-exe.bat            # Script đóng gói ứng dụng di động cho Windows
├── src/main/
│   ├── java/com/leaderboard/
│   │   ├── App.java         # Lớp khởi chạy ứng dụng chính & Splash Screen
│   │   ├── service/
│   │   │   ├── TTSService.java       # Bộ xử lý âm thanh PCM/MP3, đọc Text-To-Speech thông minh
│   │   │   ├── UpdateService.java    # Xử lý luồng kiểm tra bản cập nhật và tải về bất đồng bộ
│   │   │   └── TikTokConnector.java  # Kết nối thời gian thực với TikTok Live API
│   │   ├── ui/
│   │   │   ├── Dialogs.java          # Bộ thông báo cảnh báo Dracula theme tùy chỉnh
│   │   │   ├── DashboardLayout.java  # Bố cục giao diện dashboard chính
│   │   │   └── tab/
│   │   │       ├── OverviewTab.java  # Tab bento hiển thị tổng quan thông số live
│   │   │       └── TtsTab.java       # Tab tùy chỉnh nâng cao các thông số của bộ đọc TTS
│   └── resources/
│       ├── css/             # Tập tin định hình phong cách giao diện (Progressbar, custom css)
│       └── icons/           # Logo và bộ icon đại diện của ứng dụng
```

---

## ⚙️ Cấu Hình Cập Nhật (`update.json`)

Hệ thống cập nhật ứng dụng hoạt động dựa trên tệp metadata trực tuyến `update.json` cấu trúc như sau:

```json
{
  "version": "1.0.1",
  "downloadUrl": "https://github.com/kitakeyos-dev/TikTokStreamSuite/releases/download/v1.0.1/TikTokStreamSuite_Setup.exe",
  "changelog": "- Cải tiến tốc độ đọc bình luận tiếng Việt siêu mượt mà.\n- Cập nhật bộ lọc từ cấm ngăn ngừa vi phạm chính sách của TikTok.\n- Thiết kế lại các icon theo định dạng hình vuông 38x38 chuẩn Bento UI cực đẹp.",
  "forceUpdate": false
}
```

---

## 📝 Bản Quyền & Giấy Phép

Dự án này thuộc sở hữu của **Hoàng Hữu Dũng (kitakeyos)** và được phát hành theo Giấy phép **MIT License**. Bạn có thể tự do phát triển, sao chép và thương mại hóa theo các điều khoản trong giấy phép.

---

## 🤝 Hỗ Trợ Dự Án

Nếu bạn thấy bộ công cụ này hữu ích cho phòng live của mình, hãy tặng dự án một **⭐️ Star** trên GitHub để ủng hộ nhà phát triển nhé! 

*Chúc các streamer có những buổi phát sóng bùng nổ tương tác! 🚀*
