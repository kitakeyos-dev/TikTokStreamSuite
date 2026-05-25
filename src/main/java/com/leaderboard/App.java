package com.leaderboard;

import atlantafx.base.theme.Dracula;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.SplashScreen;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.BankConfigManager;
import com.leaderboard.util.BankDataManager;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Apply premium dark Dracula theme from AtlantaFX
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        // 1. Launch & Show the premium Splash Screen instantly!
        SplashScreen splash = new SplashScreen();
        splash.show();

        // 2. Perform heavy loading asynchronously on a background thread
        new Thread(() -> {
            try {
                // Step 1: Load config.json
                splash.setStatus("Đang tải cấu hình ứng dụng...", 0.2);
                Thread.sleep(300); // Tiny sleep for natural aesthetic progression
                ConfigManager.load();

                // Step 2: Load data.json
                splash.setStatus("Đang khởi tạo cơ sở dữ liệu...", 0.4);
                Thread.sleep(300);
                DataManager.load();

                // Step 3: Load bank_config.json
                splash.setStatus("Đang nạp cấu hình ngân hàng...", 0.6);
                Thread.sleep(300);
                BankConfigManager.load();

                // Step 4: Load bank_data.json
                splash.setStatus("Đang tải cơ sở dữ liệu giao dịch...", 0.8);
                Thread.sleep(300);
                BankDataManager.load();

                // Step 5: Dựng giao diện chính
                splash.setStatus("Đang chuẩn bị màn hình điều khiển...", 0.95);
                Thread.sleep(200);

                // Re-transition to JavaFX thread to safely construct DashboardStage
                Platform.runLater(() -> {
                    try {
                        DashboardStage dashboard = new DashboardStage();

                        // Premium fade transition to cleanly transition from splash to dashboard
                        FadeTransition fade = new FadeTransition(Duration.millis(500), splash.getScene().getRoot());
                        fade.setFromValue(1.0);
                        fade.setToValue(0.0);
                        fade.setOnFinished(event -> {
                            splash.close();
                            dashboard.show();
                        });
                        fade.play();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.exit();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.exit();
            }
        }).start();
    }

    public static void main(String[] args) {
        // Fire JavaFX Runtime
        launch(args);
    }
}
