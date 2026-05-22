@echo off
echo ===================================================
echo   TikTok Live Stream Suite - EXE Build Script
echo ===================================================

echo [1/4] Cleaning and Packaging Fat JAR via Maven...
call mvn clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven build failed!
    exit /b %ERRORLEVEL%
)

echo [2/4] Preparing staging directory...
if exist target\staging rmdir /s /q target\staging
mkdir target\staging
copy target\tiktok-leaderboard-1.0-SNAPSHOT-jar-with-dependencies.jar target\staging\

echo [3/4] Running jpackage to build portable App Image...
if exist dist rmdir /s /q dist
mkdir dist

jpackage --type app-image ^
  --dest dist ^
  --name "TikTokStreamSuite" ^
  --input target\staging ^
  --main-jar tiktok-leaderboard-1.0-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class com.leaderboard.App ^
  --icon src\main\resources\icons\logo.ico ^
  --vendor "Kitak" ^
  --app-version "1.0.0"

if %ERRORLEVEL% neq 0 (
    echo [ERROR] jpackage failed!
    exit /b %ERRORLEVEL%
)

echo ===================================================
echo   BUILD SUCCESSFUL!
echo   Portable app folder is located in: dist\TikTokStreamSuite
echo   Launch by running: dist\TikTokStreamSuite\TikTokStreamSuite.exe
echo ===================================================
