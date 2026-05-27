# TikTokStreamSuite

**English** | [Tiếng Việt](README_vi.md)

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg?style=flat-square)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg?style=flat-square)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

A TikTok Live dashboard app built with Java 17 + JavaFX 21. Dark theme (AtlantaFX Dracula).

---

## Features

### 🔊 Text-to-Speech (TTS)
- Reads live chat comments using Google Translate TTS. Audio is decoded in-memory, no temp files.
- Select audio output device (useful to route TTS separately from the stream).
- Auto-speeds up when chat is busy (backlog > 2 → 1.25x, > 4 → 1.5x).
- Profanity filter, optional nickname reading before comments.

### 📊 Leaderboard & Stats
- Overview tab: viewers, likes, gifts, connection status.
- Leaderboard: top gifters and likers, updated in real-time.
- Likes tab: track like milestones.
- Team tab: track team competition progress.
- Chat tab: view all comments with filtering.

### ⚙️ Action Rules
- Define rules: trigger (gift / like / follow / comment) → action (play audio, show overlay, run command).
- Manage rules in the Actions & Events tab.

### 🎬 Overlays
- Display overlay windows for live event alerts.
- Manage via the Overlays tab.

### 🔄 Auto-Update
- Checks for new versions on GitHub, downloads the installer, closes the app, and runs the installer automatically.

---

## Requirements

- Windows 10/11 (64-bit)
- JDK 17+ (if running from source)
- Internet connection

---

## Running from Source

```bash
git clone https://github.com/kitakeyos-dev/TikTokStreamSuite.git
cd TikTokStreamSuite

# Compile
mvn clean compile

# Run
mvn javafx:run

# Package as .exe (Windows)
build-exe.bat
```

After `build-exe.bat`, the `dist\TikTokStreamSuite\` folder contains a standalone exe with no Java requirement.

---

## Project Structure

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

## Architecture

- **ServiceLocator** — central registry, resolves implementations by interface.
- **StreamSessionMediator** — receives TikTok events and routes them to UI tabs, no direct coupling.
- **Facade** (`TTSService`, `TikTokConnector`, `ActionRulesEngine`) — static wrappers so existing code doesn't need to call ServiceLocator directly.
- **BaseDataTab\<T\>** — shared base class for data table tabs.

---

## Auto-Update (`update.json`)

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
