@echo off
setlocal
echo ===================================================
echo   TikTok Live Stream Suite - EXE Build Script
echo ===================================================

REM === JavaFX jmods stored at project root (NOT inside target/ to survive mvn clean) ===
set "JAVAFX_JMODS=%~dp0javafx-jmods"
set "JAVA_HOME_JMODS=%JAVA_HOME%\jmods"

echo [SETUP] Checking JavaFX jmods...
if not exist "%JAVAFX_JMODS%\javafx.controls.jmod" (
    echo [INFO] JavaFX jmods not found. Auto-downloading...
    if not exist javafx-jmods.zip (
        powershell -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-jmods.zip' -OutFile 'javafx-jmods.zip' -UseBasicParsing"
        if %ERRORLEVEL% neq 0 ( echo [ERROR] Download failed! & exit /b 1 )
    )
    if exist javafx-jmods-raw rmdir /s /q javafx-jmods-raw
    powershell -Command "Expand-Archive -Path 'javafx-jmods.zip' -DestinationPath 'javafx-jmods-raw' -Force"
    if exist "%JAVAFX_JMODS%" rmdir /s /q "%JAVAFX_JMODS%"
    for /d %%d in (javafx-jmods-raw\*) do xcopy /e /i /q "%%d" "%JAVAFX_JMODS%\"
    rmdir /s /q javafx-jmods-raw
    if not exist "%JAVAFX_JMODS%\javafx.controls.jmod" ( echo [ERROR] jmods extraction failed! & exit /b 1 )
    echo [OK] JavaFX jmods ready.
) else (
    echo [OK] JavaFX jmods already present.
)

if not exist "%JAVA_HOME_JMODS%\java.base.jmod" (
    echo [ERROR] JAVA_HOME jmods not found: %JAVA_HOME_JMODS%
    exit /b 1
)

echo [1/4] Cleaning and Packaging via Maven...
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 ( echo [ERROR] Maven build failed! & exit /b %ERRORLEVEL% )

echo [2/4] Preparing staging directory...
if exist target\staging rmdir /s /q target\staging
mkdir target\staging

REM Copy thin app JAR + all dependency JARs into staging (jlink handles JavaFX modules separately)
copy target\tiktok-leaderboard-1.0-SNAPSHOT.jar target\staging\
xcopy /q target\lib\*.jar target\staging\

echo [3/4] Building custom JRE with JavaFX via jlink...
if exist target\custom-runtime rmdir /s /q target\custom-runtime

jlink ^
  --module-path "%JAVAFX_JMODS%;%JAVA_HOME_JMODS%" ^
  --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.xml,jdk.crypto.cryptoki,jdk.unsupported,javafx.controls,javafx.fxml,javafx.graphics,javafx.base ^
  --output target\custom-runtime ^
  --strip-debug ^
  --no-man-pages ^
  --no-header-files ^
  --compress=zip-6

if %ERRORLEVEL% neq 0 ( echo [ERROR] jlink failed! & exit /b %ERRORLEVEL% )

echo [4/4] Running jpackage...
if exist dist rmdir /s /q dist

jpackage --type app-image ^
  --dest dist ^
  --name "TikTokStreamSuite" ^
  --input target\staging ^
  --runtime-image target\custom-runtime ^
  --main-jar tiktok-leaderboard-1.0-SNAPSHOT.jar ^
  --main-class com.leaderboard.App ^
  --icon src\main\resources\icons\logo.ico ^
  --vendor "Hoàng Hữu Dũng" ^
  --app-version "1.0.1" ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "--add-opens=java.base/java.lang=ALL-UNNAMED" ^
  --java-options "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" ^
  --java-options "--enable-native-access=javafx.graphics,javafx.media"

if %ERRORLEVEL% neq 0 ( echo [ERROR] jpackage failed! & exit /b %ERRORLEVEL% )

echo ===================================================
echo   BUILD SUCCESSFUL!
echo   Portable app: dist\TikTokStreamSuite\TikTokStreamSuite.exe
echo ===================================================
