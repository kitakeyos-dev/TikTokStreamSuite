package com.leaderboard;

import atlantafx.base.theme.Dracula;
import com.leaderboard.ui.DashboardStage;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Apply premium dark Dracula theme from AtlantaFX
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        // Initialize and fire our new central JavaFX dashboard
        DashboardStage dashboard = new DashboardStage();
        dashboard.show();
    }

    public static void main(String[] args) {
        // Fire JavaFX Runtime
        launch(args);
    }
}
