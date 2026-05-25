# 🎥 TikTokStreamSuite (TikTok Live Stream Suite)

**English** | [Tiếng Việt](README_vi.md)

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg?style=flat-square)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg?style=flat-square)](https://openjfx.io/)
[![Dracula Theme](https://img.shields.io/badge/Theme-AtlantaFX--Dracula-purple.svg?style=flat-square)](https://github.com/mkpaz/atlantafx)
[![Build Status](https://img.shields.io/badge/Platform-Windows%20x64-blueviolet.svg?style=flat-square)](https://github.com/kitakeyos-dev/TikTokStreamSuite)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

**TikTokStreamSuite** is a professional, comprehensive, and intuitive software solution designed specifically for content creators and streamers on the **TikTok Live** platform. Built on **Java 17 & JavaFX 21**, the application features a modern Dracula dark interface (AtlantaFX Dracula) paired with a premium Bento Grid layout. This design empowers streamers to optimize live room interaction, manage leaderboards, and handle real-time chat seamlessly without the need for bloated external tools.

---

## ✨ Key Features

### 🔊 1. AI-Powered Text-To-Speech Reader (Smart Vietnamese TTS)
*   **In-Memory Processing:** Directly decodes MP3 audio streams from the Google Translate API to PCM in RAM using the `JLayer` library. No temporary audio files are written, significantly boosting response time and protecting hard drive lifespan.
*   **Flexible Mixer Routing:** Allows streamers to select the exact audio output interface (e.g., dedicated headphones or secondary sound cards), preventing TTS audio from leaking into the live stream if undesired.
*   **Dynamic Rate Scaling:** Automatically detects high-frequency chat activity (backlog > 2 or > 4 messages) and dynamically scales the reading rate from `1.0x` to `1.25x` or `1.5x` by modifying the playback frequency sample rate on the fly.
*   **Profanity & Sensitive Word Filter:** Features a built-in blocked/profanity words filter to help streamers maintain a clean and welcoming live environment.
*   **Personalized Nickname Reading:** Toggleable option to read the user's nickname before pronouncing their comment.

### 📊 2. Real-Time Analytics & Leaderboard Management
*   **Overview Bento Tab:** A stylish Bento grid interface displaying core metrics (current viewers, likes, total gifts, and connection status).
*   **Leaderboard Tab:** A live leaderboard honoring top supporters (top gifters, top interactive likers) updated second-by-second.
*   **Team & Likes Tracker:** Tracks team match progress and quantifies like milestones in real-time.
*   **Chat Tab:** A centralized message stream displaying comments in a clean, modern font, complete with interactive filtering options.

### 🚀 3. Silent Auto-Updater
*   **Asynchronous Checker:** Silently checks for new versions from the GitHub Metadata server using a secure API protocol without interrupting the application's responsiveness.
*   **Indigo Premium Progress UI:** A transparent Dracula-Indigo progress interface displaying download percentage in real-time.
*   **Zero-Lock Installer Trigger:** Upon completing the `.exe` installer download, the main Java process terminates automatically to release file locks, launching the installer for a seamless, completely hands-free upgrade experience.

---

## 🛠️ System Requirements & Installation

*   **Operating System:** Windows 10/11 (64-bit).
*   **Java Runtime:** JDK 17 or higher (if running from source).
*   **Internet Connection:** Required for connecting to the TikTok Live API and Google Translate TTS API.

---

## 💻 Building & Running from Source

The project is fully managed using **Maven**. You can easily clone, build, and package the application into a standalone Windows executable using the instructions below.

### Step 1: Clone the Repository
```bash
git clone https://github.com/kitakeyos-dev/TikTokStreamSuite.git
cd TikTokStreamSuite
```

### Step 2: Compile the Application
Use Maven to download dependencies and compile the JavaFX application:
```bash
mvn clean compile
```

### Step 3: Run in Development Mode
Launch the application directly using the JavaFX plugin:
```bash
mvn javafx:run
```

### Step 4: Package as a Standalone Installer (`.exe`) for Windows
The application includes a professional packaging script `build-exe.bat` that uses **jlink** and **jpackage** to bundle a lightweight JRE with JavaFX. Simply run:
```cmd
build-exe.bat
```
*After successful execution, the `dist\TikTokStreamSuite\` directory will contain a self-contained executable that runs smoothly without requiring Java to be pre-installed on the host machine.*

---

## 📂 Project Structure

```text
TikTokStreamSuite/
├── .github/                 # GitHub Actions configuration & workflows
├── javafx-jmods.zip         # JavaFX JMods package used for jpackage bundling
├── pom.xml                  # Maven configuration file (Retrofit, JLayer, AtlantaFX, OkHttp...)
├── build-exe.bat            # Windows application packaging script
├── src/main/
│   ├── java/com/leaderboard/
│   │   ├── App.java         # Main entry point & Splash Screen
│   │   ├── service/
│   │   │   ├── TTSService.java       # PCM/MP3 audio processor & smart TTS service
│   │   │   ├── UpdateService.java    # Asynchronous update checker & downloader service
│   │   │   └── TikTokConnector.java  # Real-time TikTok Live API connector
│   │   ├── ui/
│   │   │   ├── Dialogs.java          # Custom Dracula-themed warning & dialog utilities
│   │   │   ├── DashboardLayout.java  # Main dashboard layout container
│   │   │   └── tab/
│   │   │       ├── OverviewTab.java  # Bento-style overview tab
│   │   │       └── TtsTab.java       # Advanced TTS configuration tab
│   └── resources/
│       ├── css/             # Stylesheets (ProgressBar, custom css styling)
│       └── icons/           # Logo and application icons
```

---

## ⚙️ Auto-Update Configuration (`update.json`)

The automatic updater reads a remote `update.json` metadata file structured as follows:

```json
{
  "version": "1.0.1",
  "downloadUrl": "https://github.com/kitakeyos-dev/TikTokStreamSuite/releases/download/v1.0.1/TikTokStreamSuite_Setup.exe",
  "changelog": "- Improved Vietnamese comment reading speed for ultra-smooth playback.\n- Updated profanity filters to ensure compliance with TikTok policies.\n- Redesigned icons to 38x38 square layout matching the sleek Bento UI.",
  "forceUpdate": false
}
```

---

## 📝 License

This project is owned by **Hoàng Hữu Dũng (kitakeyos)** and released under the **MIT License**. You are free to modify, distribute, and commercialize the code in accordance with the license terms.

---

## 🤝 Support the Project

If you find this suite helpful for your live streams, please consider giving it a **⭐️ Star** on GitHub to support the developer!

*Wishing all streamers a highly engaging and successful broadcast! 🚀*
