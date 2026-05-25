package com.leaderboard.service;

import com.google.gson.Gson;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.util.ConfigManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UpdateService {
    public static final String CURRENT_VERSION = "1.0.1";
    
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    private static final Gson GSON = new Gson();

    public static class UpdateInfo {
        public String version;
        public String downloadUrl;
        public String changelog;
        public boolean forceUpdate;
    }

    /**
     * Checks for updates asynchronously from the configured metadata URL.
     * @param owner Parent window for modals (can be null for background splash screen check)
     * @param silent If true, only prompts the user if a new version is found. If false, alerts them if they are up to date.
     */
    public static void checkForUpdates(Window owner, boolean silent) {
        String metadataUrl = ConfigManager.getConfig().getUpdateMetadataUrl();
        if (metadataUrl == null || metadataUrl.trim().isEmpty()) {
            if (!silent) {
                Platform.runLater(() -> Dialogs.error(owner, "Lỗi cập nhật", "Đường dẫn kiểm tra cập nhật chưa được cấu hình!"));
            }
            return;
        }

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(metadataUrl)
                        .header("User-Agent", "Mozilla/5.0")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        UpdateInfo info = GSON.fromJson(json, UpdateInfo.class);
                        
                        if (info != null && info.version != null) {
                            if (isNewerVersion(CURRENT_VERSION, info.version)) {
                                Platform.runLater(() -> promptUpdate(owner, info));
                            } else {
                                if (!silent) {
                                    Platform.runLater(() -> Dialogs.info(owner, "Cập nhật ứng dụng", 
                                            "Bạn đang sử dụng phiên bản mới nhất (v" + CURRENT_VERSION + ")!"));
                                }
                            }
                        }
                    } else {
                        if (!silent) {
                            Platform.runLater(() -> Dialogs.error(owner, "Lỗi kết nối", 
                                    "Không thể kết nối tới máy chủ cập nhật! Mã lỗi: " + response.code()));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error checking for updates: " + e.getMessage());
                if (!silent) {
                    Platform.runLater(() -> Dialogs.error(owner, "Lỗi kết nối", 
                            "Gặp lỗi khi kiểm tra cập nhật: " + e.getMessage()));
                }
            }
        }).start();
    }

    /**
     * Checks if online version is newer than current version using semantic versioning.
     */
    public static boolean isNewerVersion(String current, String online) {
        if (current == null || online == null) return false;
        String[] currParts = current.replaceAll("[^0-9.]", "").split("\\.");
        String[] onlineParts = online.replaceAll("[^0-9.]", "").split("\\.");
        
        int length = Math.max(currParts.length, onlineParts.length);
        for (int i = 0; i < length; i++) {
            int currVal = i < currParts.length ? Integer.parseInt(currParts[i]) : 0;
            int onlineVal = i < onlineParts.length ? Integer.parseInt(onlineParts[i]) : 0;
            
            if (onlineVal > currVal) return true;
            if (currVal > onlineVal) return false;
        }
        return false;
    }

    private static void promptUpdate(Window owner, UpdateInfo info) {
        String message = "Phát hiện phiên bản mới: v" + info.version + "\n\n" +
                "Nội dung cập nhật:\n" + (info.changelog != null ? info.changelog : "Không có mô tả chi tiết.") + "\n\n" +
                "Bạn có muốn tải về trình cài đặt và tự động nâng cấp ứng dụng ngay bây giờ không?";
        
        boolean confirm = Dialogs.confirm(owner, "Phát hiện Bản cập nhật mới", message, "Cập nhật ngay");
        if (confirm) {
            startDownloadProcess(owner, info.downloadUrl, info.version);
        }
    }

    private static void startDownloadProcess(Window owner, String downloadUrl, String newVersion) {
        // Create custom Dracula-themed Download Stage
        Stage downloadStage = new Stage(StageStyle.TRANSPARENT);
        downloadStage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            downloadStage.initOwner(owner);
        }
        downloadStage.setResizable(false);
        downloadStage.setAlwaysOnTop(true);

        // Header Title & Icon
        FontIcon infoIcon = new FontIcon(Feather.DOWNLOAD);
        infoIcon.setIconSize(22);
        infoIcon.setIconColor(Color.web("#818cf8"));

        Label titleLabel = new Label("Đang tải bản cập nhật (v" + newVersion + ")");
        titleLabel.setStyle("-fx-text-fill: #f4f4f5; -fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Segoe UI', system-ui;");

        HBox header = new HBox(10, infoIcon, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        // Progress Bar & Info Label
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(350);
        progressBar.setMaxWidth(350);
        
        java.net.URL progressCss = UpdateService.class.getResource("/css/progressbar.css");
        if (progressCss != null) {
            progressBar.getStylesheets().add(progressCss.toExternalForm());
        } else {
            // Fallback inline styling matching indigo gradient
            progressBar.setStyle("-fx-accent: #818cf8;");
        }

        Label statusLabel = new Label("Đang chuẩn bị kết nối...");
        statusLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px; -fx-font-family: 'Segoe UI', system-ui;");

        Label sizeLabel = new Label("0 MB / 0 MB");
        sizeLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px; -fx-font-family: 'Segoe UI', system-ui;");

        HBox labelsBox = new HBox(statusLabel, new Label("  "), sizeLabel);
        labelsBox.setAlignment(Pos.CENTER_LEFT);

        VBox contentBox = new VBox(14, header, progressBar, labelsBox);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle(
                "-fx-background-color: #121214;" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.06);" +
                "-fx-border-radius: 12px;" +
                "-fx-border-width: 1px;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.5));
        shadow.setRadius(20);
        contentBox.setEffect(shadow);

        StackPane root = new StackPane(contentBox);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        downloadStage.setScene(scene);
        downloadStage.show();

        // Download Task on separate thread
        new Thread(() -> {
            File tempInstaller = null;
            try {
                Request request = new Request.Builder()
                        .url(downloadUrl)
                        .header("User-Agent", "Mozilla/5.0")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Server returned HTTP error " + response.code());
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        throw new IOException("Response body is empty");
                    }

                    long contentLength = body.contentLength();
                    InputStream inputStream = body.byteStream();

                    tempInstaller = File.createTempFile("stream_suite_setup_", ".exe");
                    tempInstaller.deleteOnExit();

                    try (FileOutputStream outputStream = new FileOutputStream(tempInstaller)) {
                        byte[] buffer = new byte[8192];
                        long totalBytesRead = 0;
                        int bytesRead;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;

                            final double progress = contentLength > 0 ? (double) totalBytesRead / contentLength : -1;
                            final String sizeInfo = formatSize(totalBytesRead) + " / " + (contentLength > 0 ? formatSize(contentLength) : "Không rõ");
                            final int percent = contentLength > 0 ? (int) (progress * 100) : 0;

                            Platform.runLater(() -> {
                                progressBar.setProgress(progress);
                                statusLabel.setText("Đang tải bản cập nhật: " + percent + "%");
                                sizeLabel.setText("(" + sizeInfo + ")");
                            });
                        }
                    }

                    // Success! Start Installer and Exit App
                    Platform.runLater(() -> {
                        progressBar.setProgress(1.0);
                        statusLabel.setText("Đang khởi chạy trình cài đặt mới...");
                        downloadStage.close();
                    });

                    // Small delay to ensure stage closed
                    Thread.sleep(500);

                    // Launch setup file
                    ProcessBuilder pb = new ProcessBuilder(tempInstaller.getAbsolutePath());
                    pb.start();

                    // Shut down main application
                    Platform.exit();
                    System.exit(0);

                }
            } catch (Exception e) {
                e.printStackTrace();
                final File fileToDelete = tempInstaller;
                Platform.runLater(() -> {
                    downloadStage.close();
                    Dialogs.error(owner, "Lỗi tải xuống", "Không thể tải tệp tin cài đặt: " + e.getMessage());
                    if (fileToDelete != null && fileToDelete.exists()) {
                        fileToDelete.delete();
                    }
                });
            }
        }).start();
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
