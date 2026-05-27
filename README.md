# 🎥 TikTokStreamSuite (TikTok Live Stream Suite)

**English** | [Tiếng Việt](README_vi.md)

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg?style=flat-square)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg?style=flat-square)](https://openjfx.io/)
[![Dracula Theme](https://img.shields.io/badge/Theme-AtlantaFX--Dracula-purple.svg?style=flat-square)](https://github.com/mkpaz/atlantafx)
[![Build Status](https://img.shields.io/badge/Platform-Windows%20x64-blueviolet.svg?style=flat-square)](https://github.com/kitakeyos-dev/TikTokStreamSuite)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

**TikTokStreamSuite** is a professional, comprehensive, and intuitive software solution designed specifically for content creators and streamers on the **TikTok Live** platform. Built on **Java 17 & JavaFX 21**, the application features a modern Dracula dark interface (AtlantaFX Dracula) paired with a premium Bento Grid layout. It empowers streamers to optimize live room interaction, manage leaderboards, automate actions, and handle real-time chat — all without the need for bloated external tools.

---

## ✨ Key Features

### 🔊 1. AI-Powered Text-To-Speech Reader (Smart Vietnamese TTS)
*   **In-Memory Processing:** Directly decodes MP3 audio streams from the Google Translate API to PCM in RAM using the `JLayer` library. No temporary audio files are written, boosting response time and protecting drive lifespan.
*   **Flexible Mixer Routing:** Allows streamers to select the exact audio output interface (e.g., dedicated headphones or secondary sound cards), preventing TTS audio from leaking into the live stream.
*   **Dynamic Rate Scaling:** Automatically detects high-frequency chat activity (backlog > 2 or > 4 messages) and dynamically scales reading speed from `1.0x` to `1.25x` or `1.5x` by modifying the playback sample rate on the fly.
*   **Profanity & Sensitive Word Filter:** Built-in blocked/profanity word filter to maintain a clean and welcoming live environment.
*   **Personalized Nickname Reading:** Toggleable option to read the user's nickname before their comment.

### 📊 2. Real-Time Analytics & Leaderboard Management
*   **Overview Tab:** A Bento grid dashboard displaying core live metrics — current viewers, likes, total gifts, and connection status.
*   **Leaderboard Tab:** Live leaderboard honoring top gifters and top interactive likers, updated second-by-second.
*   **Likes Tab:** Tracks like milestones with real-time quantified progress towards defined goals.
*   **Team Tab:** Tracks team match progress and individual member contributions.
*   **Chat Tab:** Centralized message stream displaying comments in a clean, modern font with filtering support.

### ⚙️ 3. Automated Action Rules Engine
*   **Rule-Based Automation:** Define triggers (gifts, likes, follows, comments) mapped to actions such as playing audio, triggering overlays, or executing custom commands.
*   **Actions & Events Tab:** Full UI management for creating, editing, and enabling/disabling stream rules with a clean table interface.
*   **Decoupled Engine:** The `ActionRulesEngine` runs via `ServiceLocator`, fully isolated from UI logic for clean testability.

### 🎬 4. Live Overlay System
*   **Overlay Manager:** Independently manage and display overlay windows for alerts and real-time event notifications directly on stream.
*   **Overlays Tab:** Control overlay visibility, positioning, and content directly from the dashboard without interrupting the live session.

### 🚀 5. Silent Auto-Updater
*   **Asynchronous Checker:** Silently checks for new versions from the GitHub Metadata server using a secure API without blocking the UI.
*   **Indigo Premium Progress UI:** A transparent Dracula-Indigo progress interface displaying download percentage in real-time.
*   **Zero-Lock Installer Trigger:** After completing the `.exe` installer download, the main Java process terminates automatically to release file locks, launching the installer for a seamless, hands-free upgrade experience.

---

## 🛠️ System Requirements & Installation

*   **Operating System:** Windows 10/11 (64-bit).
*   **Java Runtime:** JDK 17 or higher (if running from source).
*   **Internet Connection:** Required for TikTok Live API and Google Translate TTS API.

---

## 💻 Building & Running from Source

The project is fully managed using **Maven**.

### Step 1: Clone the Repository
```bash
git clone https://github.com/kitakeyos-dev/TikTokStreamSuite.git
cd TikTokStreamSuite
```

### Step 2: Compile the Application
```bash
mvn clean compile
```

### Step 3: Run in Development Mode
```bash
mvn javafx:run
```

### Step 4: Package as a Standalone Installer (`.exe`) for Windows
The project includes `build-exe.bat`, which uses **jlink** and **jpackage** to bundle a lightweight JRE with JavaFX:
```cmd
build-exe.bat
```
*After successful execution, `dist\TikTokStreamSuite\` contains a self-contained executable that runs without requiring Java pre-installed on the host machine.*

---

## 📂 Project Structure

```text
TikTokStreamSuite/
├── .github/                    # GitHub Actions configuration & workflows
├── pom.xml                     # Maven config (Retrofit, JLayer, AtlantaFX, OkHttp...)
├── build-exe.bat               # Windows packaging script (jlink + jpackage)
├── update.json                 # Remote auto-update metadata
├── src/main/
│   ├── java/com/leaderboard/
│   │   ├── App.java                        # Main entry point & Splash Screen
│   │   ├── model/
│   │   │   ├── Gifter.java                 # Gift event data model
│   │   │   ├── Liker.java                  # Like event data model
│   │   │   ├── TeamMember.java             # Team member data model
│   │   │   └── action/
│   │   │       ├── StreamRule.java         # Automation rule entity (trigger → action)
│   │   │       ├── TriggerType.java        # Enum: gift, like, follow, comment
│   │   │       └── ActionType.java         # Enum: play audio, overlay, command...
│   │   ├── service/
│   │   │   ├── ServiceLocator.java         # Central IoC / service registry
│   │   │   ├── StreamSessionMediator.java  # Mediator: decouples UI from TikTok events
│   │   │   ├── ITTSService.java            # TTS service interface
│   │   │   ├── TTSService.java             # Static facade for TTS
│   │   │   ├── ITikTokConnector.java       # Connector interface
│   │   │   ├── TikTokConnector.java        # Static facade for TikTok connector
│   │   │   ├── IActionRulesEngine.java     # Rules engine interface
│   │   │   ├── ActionRulesEngine.java      # Static facade for rules engine
│   │   │   ├── UpdateService.java          # Async update checker & downloader
│   │   │   └── impl/
│   │   │       ├── TTSServiceImpl.java          # TTS implementation (audio decode & playback)
│   │   │       ├── TikTokConnectorImpl.java     # TikTok Live WebSocket implementation
│   │   │       └── ActionRulesEngineImpl.java   # Rule matching & action dispatch
│   │   ├── ui/
│   │   │   ├── DashboardStage.java         # Primary application window & service bootstrap
│   │   │   ├── DashboardLayout.java        # Bento-style main layout container
│   │   │   ├── Dialogs.java                # Dracula-themed dialog & alert utilities
│   │   │   ├── SplashScreen.java           # Animated startup splash screen
│   │   │   ├── ToggleSwitch.java           # Custom toggle switch control
│   │   │   ├── overlay/                    # Overlay window components
│   │   │   └── tab/
│   │   │       ├── BaseDataTab.java        # Generic base class for data-bound tabs
│   │   │       ├── OverviewTab.java        # Bento overview metrics tab
│   │   │       ├── LeaderboardTab.java     # Real-time leaderboard tab
│   │   │       ├── LikesTab.java           # Like milestone tracker tab
│   │   │       ├── TeamTab.java            # Team competition tracker tab
│   │   │       ├── ChatTab.java            # Live chat message stream tab
│   │   │       ├── ActionsEventsTab.java   # Stream automation rules UI tab
│   │   │       ├── OverlaysTab.java        # Overlay manager tab
│   │   │       └── TtsTab.java             # Advanced TTS configuration tab
│   │   └── util/
│   │       ├── ConfigManager.java          # Persistent app settings (JSON)
│   │       ├── DataManager.java            # In-memory runtime data store
│   │       ├── EmojiParser.java            # Emoji-to-text conversion
│   │       ├── I18n.java                   # Localization utility
│   │       ├── IconManager.java            # Icon resource loader
│   │       └── ResizeHelper.java           # Borderless window resize handler
│   └── resources/
│       ├── css/                            # Custom stylesheets (progress bar, UI overrides)
│       └── icons/                          # Application logo & icons
```

---

## 🏗️ Architecture Overview

The application is built on a clean, layered architecture:

| Pattern | Class | Role |
|---|---|---|
| **Service Locator** | `ServiceLocator` | Central registry — resolves service implementations by interface |
| **Mediator** | `StreamSessionMediator` | Routes TikTok events to UI tabs without direct coupling |
| **Facade** | `TTSService`, `TikTokConnector`, `ActionRulesEngine` | Static convenience wrappers over `ServiceLocator` |
| **Interface Segregation** | `ITTSService`, `ITikTokConnector`, `IActionRulesEngine` | Service contracts decoupled from implementation |
| **Template Method** | `BaseDataTab<T>` | Shared boilerplate for data-bound table tabs |

---

## ⚙️ Auto-Update Configuration (`update.json`)

The automatic updater reads a remote `update.json` metadata file structured as follows:

```json
{
  "version": "1.0.1",
  "downloadUrl": "https://github.com/kitakeyos-dev/TikTokStreamSuite/releases/download/v1.0.1/TikTokStreamSuite_Setup.exe",
  "changelog": "- Improved Vietnamese comment reading speed.\n- Updated profanity filters.\n- Redesigned icons to 38x38 square layout.",
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
